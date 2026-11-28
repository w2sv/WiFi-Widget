package com.w2sv.wifiwidget.ui.location.capability.access

import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.w2sv.augmentedpermissions.PermissionRequestHistory
import com.w2sv.domain.location.IsLocationEnabled
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RememberLocationAccessCapabilityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `provider stays live and replacing it creates a new access capability`() {
        var enabled = false
        val provider = mutableStateOf(locationEnabled { enabled })
        val recompose = mutableStateOf(false)
        val requestHistory = PermissionRequestHistory(MutableStateFlow(false)) {}
        var capability: LocationAccessCapability? = null
        composeRule.setContent {
            recompose.value
            capability = rememberLocationAccessCapability(
                isLocationEnabled = provider.value,
                requestHistory = requestHistory
            )
        }

        val original = capability!!
        composeRule.runOnIdle { assertFalse(original.locationServicesEnabled) }
        enabled = true
        composeRule.runOnIdle {
            assertTrue(original.locationServicesEnabled)
        }
        composeRule.runOnIdle { recompose.value = true }
        composeRule.runOnIdle { assertSame(original, capability) }

        composeRule.runOnIdle { provider.value = locationEnabled { false } }
        composeRule.runOnIdle {
            assertNotSame(original, capability)
            assertFalse(capability.locationServicesEnabled)
        }
    }

    @Test
    fun `opens Android location settings`() {
        var capability: LocationAccessCapability? = null
        composeRule.setContent {
            capability = rememberLocationAccessCapability(
                isLocationEnabled = locationEnabled { true },
                requestHistory = PermissionRequestHistory(MutableStateFlow(false)) {}
            )
        }

        composeRule.runOnIdle { capability!!.openLocationSettings() }
        val intent = shadowOf(composeRule.activity).nextStartedActivity
        assertEquals(Settings.ACTION_LOCATION_SOURCE_SETTINGS, intent.action)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    private fun locationEnabled(read: () -> Boolean): IsLocationEnabled =
        object : IsLocationEnabled {
            override fun invoke(): Boolean =
                read()
            override val flow: Flow<Boolean> = flowOf(read())
        }
}
