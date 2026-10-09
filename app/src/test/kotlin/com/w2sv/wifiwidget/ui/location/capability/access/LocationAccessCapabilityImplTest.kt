package com.w2sv.wifiwidget.ui.location.capability.access

import com.w2sv.wifiwidget.ui.location.OnLocationAccessGranted.TriggerWidgetDataRefresh
import com.w2sv.wifiwidget.ui.location.capability.permission.LocationPermissionCapability
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationAccessCapabilityImplTest {

    @Test
    fun `reads the current location state and opens settings`() {
        var enabled = false
        var settingsOpenCount = 0
        val capability = LocationAccessCapabilityImpl(
            locationEnabledProvider = { enabled },
            openSettings = { settingsOpenCount++ },
            permissionCapability = mockk(relaxed = true)
        )

        assertFalse(capability.isLocationEnabled)
        enabled = true
        assertTrue(capability.isLocationEnabled)
        capability.openLocationSettings()
        assertEquals(1, settingsOpenCount)
    }

    @Test
    fun `delegates permission state actions and grant events`() =
        runTest {
            val events = flowOf(TriggerWidgetDataRefresh)
            val permissionCapability = mockk<LocationPermissionCapability>(relaxed = true) {
                every { foregroundPermissionsGranted } returns true
                every { isBackgroundPermissionMissing } returns true
                every { showForegroundRational } returns true
                every { showBackgroundRational } returns false
                every { grantEvents } returns events
            }
            val capability = LocationAccessCapabilityImpl(
                locationEnabledProvider = { true },
                openSettings = {},
                permissionCapability = permissionCapability
            )

            assertTrue(capability.foregroundPermissionsGranted)
            assertTrue(capability.isBackgroundPermissionMissing)
            assertTrue(capability.showForegroundRational)
            assertFalse(capability.showBackgroundRational)
            assertSame(events, capability.grantEvents)

            capability.onForegroundRationalProceed()
            capability.launchBackgroundPermission()
            capability.dismissBackgroundRational()
            capability.requestPermission(TriggerWidgetDataRefresh)
            capability.onPermissionGranted()

            verify(exactly = 1) { permissionCapability.onForegroundRationalProceed() }
            verify(exactly = 1) { permissionCapability.launchBackgroundPermission() }
            verify(exactly = 1) { permissionCapability.dismissBackgroundRational() }
            verify(exactly = 1) { permissionCapability.requestPermission(TriggerWidgetDataRefresh) }
            coVerify(exactly = 1) { permissionCapability.onPermissionGranted() }
        }
}
