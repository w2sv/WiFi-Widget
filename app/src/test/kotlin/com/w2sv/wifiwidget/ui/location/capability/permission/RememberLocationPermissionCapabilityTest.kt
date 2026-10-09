package com.w2sv.wifiwidget.ui.location.capability.permission

import android.Manifest
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import com.w2sv.augmentedpermissions.PermissionRequestHistory
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.location.LocationAccessRationalHistory
import com.w2sv.wifiwidget.ui.location.OnLocationAccessGranted
import com.w2sv.wifiwidget.ui.location.OnLocationAccessGranted.TriggerWidgetDataRefresh
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class RememberLocationPermissionCapabilityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    @Config(sdk = [35])
    fun `collected rational history updates a stable capability`() {
        val rationalWasShown = MutableStateFlow(true)
        val recompose = mutableStateOf(false)
        var capability: LocationPermissionCapability? = null
        composeRule.setContent {
            recompose.value
            capability = rememberLocationPermissionCapability(
                requestHistory = PermissionRequestHistory(MutableStateFlow(false)) {},
                rationalHistory = LocationAccessRationalHistory(rationalWasShown) {}
            )
        }

        val original = capability!!
        composeRule.runOnIdle { assertFalse(original.showForegroundRational) }
        rationalWasShown.value = false
        composeRule.waitUntil { original.showForegroundRational }
        composeRule.runOnIdle { recompose.value = true }
        composeRule.runOnIdle { assertSame(original, capability) }
    }

    @Test
    @Config(sdk = [28])
    fun `background permission is absent before Android Q`() {
        var capability: LocationPermissionCapability? = null
        composeRule.setContent {
            capability = rememberLocationPermissionCapability(
                requestHistory = PermissionRequestHistory(MutableStateFlow(false)) {},
                rationalHistory = LocationAccessRationalHistory(MutableStateFlow(true)) {}
            )
        }

        composeRule.runOnIdle { assertFalse(capability!!.isBackgroundPermissionMissing) }
    }

    @Test
    @Config(sdk = [35])
    fun `background permission is required from Android Q`() {
        var capability: LocationPermissionCapability? = null
        composeRule.setContent {
            capability = rememberLocationPermissionCapability(
                requestHistory = PermissionRequestHistory(MutableStateFlow(false)) {},
                rationalHistory = LocationAccessRationalHistory(MutableStateFlow(true)) {}
            )
        }

        composeRule.runOnIdle { assertTrue(capability!!.isBackgroundPermissionMissing) }
    }

    @Test
    @Config(sdk = [35])
    fun `persisted foreground request history routes suppressed requests to settings snackbar`() {
        val snackbarHostState = SnackbarHostState()
        var capability: LocationPermissionCapability? = null
        composeRule.setContent {
            CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
                capability = rememberLocationPermissionCapability(
                    requestHistory = PermissionRequestHistory(MutableStateFlow(true)) {},
                    rationalHistory = LocationAccessRationalHistory(MutableStateFlow(true)) {}
                )
            }
        }

        composeRule.runOnIdle { capability!!.requestPermission() }
        composeRule.waitUntil { snackbarHostState.currentSnackbarData != null }
        composeRule.runOnIdle {
            val visuals = snackbarHostState.currentSnackbarData!!.visuals as AppSnackbarVisuals
            assertEquals(
                composeRule.activity.getString(R.string.you_need_to_go_to_the_app_settings_and_grant_location_access_permission),
                visuals.message
            )
            assertEquals(composeRule.activity.getString(R.string.go_to_app_settings), visuals.actionLabel)
            assertNull(shadowOf(composeRule.activity).lastRequestedPermission)
            visuals.action!!.callback()
        }
        val intent = shadowOf(composeRule.activity).nextStartedActivity
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
        assertEquals("package:${composeRule.activity.packageName}", intent.dataString)
    }

    @Test
    @Config(sdk = [35])
    fun `first foreground request launches both platform permissions and records history`() {
        var requestRecordedCount = 0
        var capability: LocationPermissionCapability? = null
        composeRule.setContent {
            capability = rememberLocationPermissionCapability(
                requestHistory = PermissionRequestHistory(MutableStateFlow(false)) { requestRecordedCount++ },
                rationalHistory = LocationAccessRationalHistory(MutableStateFlow(true)) {}
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
    fun `foreground grant after initial denial emits the pending action`() {
        val events = MutableStateFlow<List<OnLocationAccessGranted>>(emptyList())
        var capability: LocationPermissionCapability? = null
        composeRule.setContent {
            capability = rememberLocationPermissionCapability(
                requestHistory = PermissionRequestHistory(MutableStateFlow(true)) {},
                rationalHistory = LocationAccessRationalHistory(MutableStateFlow(true)) {}
            )
            LaunchedEffect(capability) {
                capability.grantEvents.collect { events.value += it }
            }
        }

        composeRule.runOnIdle {
            assertFalse(capability!!.foregroundPermissionsGranted)
            capability.requestPermission(TriggerWidgetDataRefresh)
        }
        shadowOf(composeRule.activity).grantPermissions(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeRule.waitUntil { capability!!.foregroundPermissionsGranted && events.value.isNotEmpty() }
        assertEquals(listOf(TriggerWidgetDataRefresh), events.value)
    }
}
