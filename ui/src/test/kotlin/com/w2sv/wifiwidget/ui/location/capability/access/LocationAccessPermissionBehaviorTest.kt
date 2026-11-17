package com.w2sv.wifiwidget.ui.location.capability.access

import android.Manifest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.turbine.test
import com.w2sv.augmentedpermissions.PermissionState
import com.w2sv.wifiwidget.ui.location.LocationPermissionLevel
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationAccessPermissionBehaviorTest {

    private val foreground = FakePermissionState()
    private val background = FakePermissionState()
    private var settingsOpenCount = 0

    private fun capability(backgroundState: PermissionState? = background) =
        LocationAccessCapabilityImpl(
            foregroundPermissionsState = foreground,
            backgroundPermissionState = backgroundState,
            launchAppSettings = { settingsOpenCount++ },
            locationEnabledProvider = mockk(relaxed = true),
            openLocationServiceSettings = {}
        )

    @Test
    fun `permission status is read when accessed`() {
        val capability = capability()

        assertEquals(LocationPermissionLevel.None, capability.permissionLevel)

        foreground.approximateOnly = true
        assertEquals(LocationPermissionLevel.ApproximateOnly, capability.permissionLevel)

        foreground.isGranted = true
        assertEquals(LocationPermissionLevel.PreciseForeground, capability.permissionLevel)

        background.isGranted = true
        assertEquals(LocationPermissionLevel.PreciseBackground, capability.permissionLevel)
        assertEquals(LocationPermissionLevel.PreciseBackground, capability(backgroundState = null).permissionLevel)

        foreground.isGranted = false
        foreground.approximateOnly = false
        assertEquals(LocationPermissionLevel.None, capability.permissionLevel)
        assertEquals(LocationPermissionLevel.None, capability(backgroundState = null).permissionLevel)
    }

    @Test
    fun `foreground request launches the permission request`() {
        capability().requestPermission()

        assertEquals(1, foreground.launchCount)
        assertEquals(0, background.launchCount)
        assertEquals(0, settingsOpenCount)
    }

    @Test
    fun `background request launches independently and exposes its result`() =
        runTest {
            val capability = capability()

            capability.backgroundRequestResults.test {
                capability.launchBackgroundPermission()
                assertEquals(1, background.launchCount)
                assertEquals(0, foreground.launchCount)
                assertEquals(0, settingsOpenCount)
                background.grantedFromRequest.emit(true)
                assertEquals(true, awaitItem())
            }
        }

    @Test
    fun `granted or unavailable background permission is not missing`() {
        foreground.isGranted = true
        background.isGranted = true
        assertEquals(LocationPermissionLevel.PreciseBackground, capability().permissionLevel)

        val unavailableBackground = capability(backgroundState = null)
        unavailableBackground.launchBackgroundPermission()
        assertEquals(LocationPermissionLevel.PreciseBackground, unavailableBackground.permissionLevel)
        assertEquals(0, background.launchCount)
    }

    @Test
    fun `foreground request results remain observable`() =
        runTest {
            capability().foregroundRequestResults.test {
                foreground.grantedFromRequest.emit(false)
                assertEquals(false, awaitItem())
                foreground.grantedFromRequest.emit(true)
                assertEquals(true, awaitItem())
            }
        }

    @Test
    fun `suppressed foreground request opens app settings`() {
        val capability = capability()
        capability.requestPermission()

        assertEquals(1, foreground.launchCount)
        foreground.onSuppressed!!.invoke()
        assertEquals(1, settingsOpenCount)
    }

    private class FakePermissionState : PermissionState {
        override var isGranted by mutableStateOf(false)
        override val shouldShowRationale = false
        override val isLaunchingSuppressed = false
        var approximateOnly by mutableStateOf(false)
        override val revokedPermissions
            get() = when {
                isGranted -> emptyList()
                approximateOnly -> listOf(Manifest.permission.ACCESS_FINE_LOCATION)
                else -> listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        override val grantedFromRequest = MutableSharedFlow<Boolean>()
        var launchCount = 0
        var onSuppressed: (() -> Unit)? = null

        override fun launchRequest(onSuppressed: (() -> Unit)?) {
            launchCount++
            this.onSuppressed = onSuppressed
        }
    }
}
