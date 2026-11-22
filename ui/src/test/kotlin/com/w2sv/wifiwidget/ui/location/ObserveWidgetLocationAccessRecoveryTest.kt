package com.w2sv.wifiwidget.ui.location

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.w2sv.domain.location.IsLocationEnabled
import com.w2sv.domain.location.LocationPermissionLevel
import com.w2sv.domain.widget.WidgetManager
import com.w2sv.wifiwidget.ui.location.capability.access.LocationAccessCapability
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ObserveWidgetLocationAccessRecoveryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val actions = mockk<WidgetManager>(relaxed = true)
    private val observer = WidgetLocationRecoveryObserver(actions)
    private val permissionLevel = mutableStateOf(LocationPermissionLevel.None)
    private val servicesFlow = MutableStateFlow(false)
    private var servicesEnabled = false
    private val locationEnabled = object : IsLocationEnabled {
        override fun invoke(): Boolean =
            servicesEnabled
        override val flow = servicesFlow
    }
    private val capability = mockk<LocationAccessCapability>(relaxed = true) {
        every { permissionLevel } answers { this@ObserveWidgetLocationAccessRecoveryTest.permissionLevel.value }
        every { locationServicesEnabledFlow } returns servicesFlow
    }

    @Test
    fun `reactive permission and services triggers always reread authoritative Android state`() {
        composeRule.setContent { ObserveWidgetLocationAccessRecovery(capability, locationEnabled, observer) }
        composeRule.runOnIdle {
            // Stale or early Compose permissions must not manufacture a recovery.
            permissionLevel.value = LocationPermissionLevel.PreciseForeground
            servicesEnabled = true
            servicesFlow.value = true
        }
        composeRule.runOnIdle { verify(exactly = 0) { actions.refresh() } }

        composeRule.runOnIdle {
            servicesEnabled = false
            grant(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            permissionLevel.value = LocationPermissionLevel.ApproximateOnly
        }
        composeRule.runOnIdle { verify(exactly = 0) { actions.refresh() } }
        composeRule.runOnIdle {
            servicesEnabled = true
            servicesFlow.value = false
        }
        composeRule.runOnIdle { verify(exactly = 1) { actions.refresh() } }

        composeRule.runOnIdle {
            grant(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            permissionLevel.value = LocationPermissionLevel.PreciseBackground
        }
        composeRule.runOnIdle { verify(exactly = 2) { actions.refresh() } }
        verify(exactly = 0) { capability.requestPermission() }
        verify(exactly = 0) { capability.openAppSettings() }
        verify(exactly = 0) { capability.openLocationSettings() }
    }

    @Test
    fun `resume detects Settings changes without reactive emissions and stops observing while inactive`() {
        val host = host()
        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides host) {
                ObserveWidgetLocationAccessRecovery(capability, locationEnabled, observer)
            }
        }
        composeRule.runOnIdle { assertEquals(1, servicesFlow.subscriptionCount.value) }
        composeRule.runOnIdle { host.lifecycle.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle {
            assertEquals(0, servicesFlow.subscriptionCount.value)
            grant(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            servicesEnabled = true
        }
        composeRule.runOnIdle { verify(exactly = 0) { actions.refresh() } }
        composeRule.runOnIdle { host.lifecycle.currentState = Lifecycle.State.RESUMED }
        composeRule.runOnIdle { verify(exactly = 1) { actions.refresh() } }
        composeRule.runOnIdle { host.lifecycle.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { host.lifecycle.currentState = Lifecycle.State.RESUMED }
        composeRule.runOnIdle { verify(exactly = 1) { actions.refresh() } }
    }

    @Test
    fun `multiple hosts and configuration recreation retain process history and deduplicate initial recovery`() {
        grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        servicesEnabled = true
        val hosts = mutableStateOf(listOf(host(), host()))
        composeRule.setContent {
            hosts.value.forEach { host ->
                key(host) {
                    CompositionLocalProvider(LocalLifecycleOwner provides host) {
                        ObserveWidgetLocationAccessRecovery(capability, locationEnabled, observer)
                    }
                }
            }
        }
        composeRule.runOnIdle { verify(exactly = 1) { actions.refresh() } }
        composeRule.runOnIdle {
            hosts.value.first().lifecycle.currentState = Lifecycle.State.DESTROYED
            hosts.value = listOf(Host(), hosts.value.last())
            hosts.value.first().lifecycle.currentState = Lifecycle.State.RESUMED
        }
        composeRule.runOnIdle { verify(exactly = 1) { actions.refresh() } }
    }

    private fun grant(vararg permissions: String) {
        shadowOf(composeRule.activity.application).grantPermissions(*permissions)
    }

    private fun host(): Host {
        val host = Host()
        composeRule.runOnUiThread { host.lifecycle.currentState = Lifecycle.State.RESUMED }
        return host
    }

    private class Host : LifecycleOwner {
        override val lifecycle = LifecycleRegistry(this)
    }
}
