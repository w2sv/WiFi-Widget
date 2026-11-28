package com.w2sv.wifiwidget.ui.location.capability.access

import android.Manifest
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import com.w2sv.augmentedpermissions.PermissionRequestHistory
import com.w2sv.domain.location.IsLocationEnabled
import com.w2sv.domain.location.LocationPermissionLevel
import com.w2sv.wifiwidget.ui.location.LocationAccessAvailability
import com.w2sv.wifiwidget.ui.screen.locationaccess.flow.LocationAccessEvent
import com.w2sv.wifiwidget.ui.screen.locationaccess.flow.LocationAccessFlowState
import com.w2sv.wifiwidget.ui.screen.locationaccess.flow.rememberLocationAccessAvailability
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class RememberLocationAccessPermissionsTest {

    private val locationEnabled = mockk<IsLocationEnabled>(relaxed = true)

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    @Config(sdk = [35])
    fun `settings reconciliation reads Android grants while availability uses the capability`() {
        shadowOf(composeRule.activity).grantPermissions(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        val capability = mockk<LocationAccessCapability> {
            every { permissionLevel } returns LocationPermissionLevel.None
            every { locationServicesEnabled } returns true
            every { locationServicesEnabledFlow } returns flowOf(true)
        }
        val flowState = MutableStateFlow<LocationAccessFlowState?>(
            LocationAccessFlowState.BackgroundSettings(PreciseForeground, hasLeftApp = true)
        )
        val events = mutableListOf<LocationAccessEvent>()
        var availability: LocationAccessAvailability? = null
        composeRule.setContent {
            availability = rememberLocationAccessAvailability(composeRule.activity, capability, flowState) {
                events += it
                null
            }
        }

        composeRule.runOnIdle {
            assertEquals(LocationPermissionLevel.None, availability!!.permissionLevel)
            assertEquals(listOf(LocationAccessEvent.BackgroundAccessAvailable), events)
        }
    }

    @Test
    @Config(sdk = [35])
    fun `recomposition keeps a stable capability`() {
        val recompose = mutableStateOf(false)
        var capability: LocationAccessCapability? = null
        composeRule.setContent {
            recompose.value
            capability = rememberLocationAccessCapability(
                isLocationEnabled = locationEnabled,
                requestHistory = PermissionRequestHistory(MutableStateFlow(false)) {}
            )
        }

        val original = capability!!
        composeRule.runOnIdle { recompose.value = true }
        composeRule.runOnIdle { assertSame(original, capability) }
    }

    @Test
    @Config(sdk = [28])
    fun `background permission is absent before Android Q`() {
        shadowOf(
            composeRule.activity
        ).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        var capability: LocationAccessCapability? = null
        composeRule.setContent {
            capability = rememberLocationAccessCapability(
                isLocationEnabled = locationEnabled,
                requestHistory = PermissionRequestHistory(MutableStateFlow(false)) {}
            )
        }

        composeRule.runOnIdle { assertEquals(LocationPermissionLevel.PreciseBackground, capability!!.permissionLevel) }
    }

    @Test
    @Config(sdk = [35])
    fun `background permission is required from Android Q`() {
        shadowOf(
            composeRule.activity
        ).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        var capability: LocationAccessCapability? = null
        composeRule.setContent {
            capability = rememberLocationAccessCapability(
                isLocationEnabled = locationEnabled,
                requestHistory = PermissionRequestHistory(MutableStateFlow(false)) {}
            )
        }

        composeRule.runOnIdle { assertEquals(LocationPermissionLevel.PreciseForeground, capability!!.permissionLevel) }
    }

    @Test
    @Config(sdk = [35])
    fun `persisted foreground request history routes suppressed requests to settings`() {
        var capability: LocationAccessCapability? = null
        composeRule.setContent {
            capability = rememberLocationAccessCapability(
                isLocationEnabled = locationEnabled,
                requestHistory = PermissionRequestHistory(MutableStateFlow(true)) {}
            )
        }

        composeRule.runOnIdle { capability!!.requestPermission() }
        assertNull(shadowOf(composeRule.activity).lastRequestedPermission)
        val intent = shadowOf(composeRule.activity).nextStartedActivity
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
        assertEquals("package:${composeRule.activity.packageName}", intent.dataString)
    }

    @Test
    @Config(sdk = [35])
    fun `first foreground request launches both platform permissions and records history`() {
        var requestRecordedCount = 0
        var capability: LocationAccessCapability? = null
        composeRule.setContent {
            capability = rememberLocationAccessCapability(
                isLocationEnabled = locationEnabled,
                requestHistory = PermissionRequestHistory(MutableStateFlow(false)) { requestRecordedCount++ }
            )
        }

        composeRule.runOnIdle { capability!!.requestPermission() }
        val requested = shadowOf(composeRule.activity).lastRequestedPermission
        assertEquals(
            setOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            requested.requestedPermissions.toSet()
        )
        assertEquals(1, requestRecordedCount)
    }

    @Test
    @Config(sdk = [35])
    fun `foreground grant after initial denial updates permission availability on resume`() {
        var capability: LocationAccessCapability? = null
        composeRule.setContent {
            capability = rememberLocationAccessCapability(
                isLocationEnabled = locationEnabled,
                requestHistory = PermissionRequestHistory(MutableStateFlow(true)) {}
            )
        }

        composeRule.runOnIdle {
            assertFalse(capability!!.permissionLevel.hasPreciseAccess)
            capability.requestPermission()
        }
        shadowOf(composeRule.activity).grantPermissions(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeRule.waitUntil { capability!!.permissionLevel == LocationPermissionLevel.PreciseForeground }
    }
}
