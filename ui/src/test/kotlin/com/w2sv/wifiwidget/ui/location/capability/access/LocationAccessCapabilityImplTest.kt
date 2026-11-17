package com.w2sv.wifiwidget.ui.location.capability.access

import app.cash.turbine.test
import com.w2sv.domain.location.IsLocationEnabled
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationAccessCapabilityImplTest {

    @Test
    fun `observes system location service changes`() =
        runTest {
            val locationEnabled = mockk<IsLocationEnabled>()
            every { locationEnabled.flow } returns flowOf(false, true)
            val capability = LocationAccessCapabilityImpl(
                locationEnabledProvider = locationEnabled,
                openLocationServiceSettings = {},
                foregroundPermissionsState = mockk(relaxed = true),
                backgroundPermissionState = null,
                launchAppSettings = {}
            )

            capability.locationServicesEnabledFlow.test {
                assertFalse(awaitItem())
                assertTrue(awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun `reads the current location state and opens settings`() {
        var enabled = false
        var settingsOpenCount = 0
        val locationEnabled = mockk<IsLocationEnabled>()
        every { locationEnabled.invoke() } answers { enabled }
        every { locationEnabled.flow } returns flowOf(false)
        val capability = LocationAccessCapabilityImpl(
            locationEnabledProvider = locationEnabled,
            openLocationServiceSettings = { settingsOpenCount++ },
            foregroundPermissionsState = mockk(relaxed = true),
            backgroundPermissionState = null,
            launchAppSettings = {}
        )

        assertFalse(capability.locationServicesEnabled)
        enabled = true
        assertTrue(capability.locationServicesEnabled)
        capability.openLocationSettings()
        assertEquals(1, settingsOpenCount)
    }
}
