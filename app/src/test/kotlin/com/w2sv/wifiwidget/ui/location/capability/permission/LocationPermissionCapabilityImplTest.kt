package com.w2sv.wifiwidget.ui.location.capability.permission

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.turbine.test
import com.w2sv.augmentedpermissions.PermissionState
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.location.OnLocationAccessGranted.EnableLocationAccessRequiringProperties
import com.w2sv.wifiwidget.ui.location.OnLocationAccessGranted.TriggerWidgetDataRefresh
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarBuilder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationPermissionCapabilityImplTest {

    private val foreground = FakePermissionState()
    private val background = FakePermissionState()
    private var rationalShown = false
    private var rationalSaveCount = 0
    private var settingsOpenCount = 0
    private var snackbarBuilder: SnackbarBuilder? = null

    private fun capability(backgroundState: PermissionState? = background) =
        LocationPermissionCapabilityImpl(
            foregroundPermissionsState = foreground,
            backgroundPermissionState = backgroundState,
            rationalShown = { rationalShown },
            saveRationalShown = { rationalSaveCount++ },
            showSnackbar = { snackbarBuilder = it },
            openAppSettings = { settingsOpenCount++ }
        )

    @Test
    fun `permission status and rational history are read when accessed`() {
        val capability = capability()

        assertFalse(capability.foregroundPermissionsGranted)
        assertTrue(capability.isBackgroundPermissionMissing)
        assertTrue(capability.showForegroundRational)

        foreground.isGranted = true
        background.isGranted = true
        rationalShown = true

        assertTrue(capability.foregroundPermissionsGranted)
        assertFalse(capability.isBackgroundPermissionMissing)
        assertFalse(capability.showForegroundRational)
        assertFalse(capability(backgroundState = null).isBackgroundPermissionMissing)
    }

    @Test
    fun `foreground rational proceeds and requests the enable action`() =
        runTest {
            val capability = capability()

            capability.onForegroundRationalProceed()

            assertEquals(1, rationalSaveCount)
            assertEquals(1, foreground.launchCount)
            capability.grantEvents.test {
                capability.onPermissionGranted()
                assertEquals(EnableLocationAccessRequiringProperties, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `latest pending action is emitted once after a grant`() =
        runTest {
            val capability = capability()
            capability.requestPermission(EnableLocationAccessRequiringProperties)
            capability.requestPermission(TriggerWidgetDataRefresh)

            capability.grantEvents.test {
                capability.onPermissionGranted()
                assertEquals(TriggerWidgetDataRefresh, awaitItem())
                capability.onPermissionGranted()
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(2, foreground.launchCount)
        }

    @Test
    fun `request without an action clears an earlier pending action`() =
        runTest {
            val capability = capability()
            capability.requestPermission(TriggerWidgetDataRefresh)
            capability.requestPermission(null)

            capability.grantEvents.test {
                capability.onPermissionGranted()
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(2, foreground.launchCount)
        }

    @Test
    fun `grant without a pending action still offers missing background permission`() =
        runTest {
            val capability = capability()

            capability.grantEvents.test {
                capability.onPermissionGranted()
                expectNoEvents()
                assertTrue(capability.showBackgroundRational)
                capability.dismissBackgroundRational()
                assertFalse(capability.showBackgroundRational)
                capability.onPermissionGranted()
                capability.launchBackgroundPermission()
                assertEquals(1, background.launchCount)
                assertFalse(capability.showBackgroundRational)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `granted or unavailable background permission never opens its rational`() =
        runTest {
            background.isGranted = true
            val grantedBackground = capability()
            grantedBackground.onPermissionGranted()
            assertFalse(grantedBackground.showBackgroundRational)

            val unavailableBackground = capability(backgroundState = null)
            unavailableBackground.onPermissionGranted()
            unavailableBackground.launchBackgroundPermission()
            assertFalse(unavailableBackground.showBackgroundRational)
            assertEquals(0, background.launchCount)
        }

    @Test
    fun `suppressed foreground request supplies settings snackbar callback`() {
        val capability = capability()
        capability.requestPermission()

        assertEquals(1, foreground.launchCount)
        foreground.onSuppressed!!.invoke()
        val context = mockk<Context> { every { getString(any()) } returns "settings" }
        val visuals = snackbarBuilder!!(context) as AppSnackbarVisuals
        assertEquals(SnackbarKind.Warning, visuals.kind)
        visuals.action!!.callback()
        assertEquals(1, settingsOpenCount)
    }

    private class FakePermissionState : PermissionState {
        override var isGranted by mutableStateOf(false)
        override val shouldShowRationale = false
        override val isLaunchingSuppressed = false
        override val revokedPermissions = emptyList<String>()
        override val grantedFromRequest: SharedFlow<Boolean> = MutableSharedFlow()
        var launchCount = 0
        var onSuppressed: (() -> Unit)? = null

        override fun launchRequest(onSuppressed: (() -> Unit)?) {
            launchCount++
            this.onSuppressed = onSuppressed
        }
    }
}
