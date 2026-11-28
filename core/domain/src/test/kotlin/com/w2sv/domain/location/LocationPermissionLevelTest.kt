package com.w2sv.domain.location

import android.Manifest
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LocationPermissionLevelTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test fun `snapshots classify current grants and require precise permission for background access`() {
        val application = shadowOf(context)
        assertEquals(LocationPermissionLevel.None, context.locationPermissionLevel())

        application.grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
        assertEquals(LocationPermissionLevel.ApproximateOnly, context.locationPermissionLevel())

        application.grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        assertEquals(LocationPermissionLevel.PreciseForeground, context.locationPermissionLevel())

        application.grantPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        assertEquals(LocationPermissionLevel.PreciseBackground, context.locationPermissionLevel())

        application.denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        assertEquals(LocationPermissionLevel.ApproximateOnly, context.locationPermissionLevel())
        application.denyPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
        assertEquals(LocationPermissionLevel.None, context.locationPermissionLevel())
    }

    @Test
    @Config(sdk = [28])
    fun `precise access includes background access when no separate background permission is required`() {
        shadowOf(context).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

        assertEquals(LocationPermissionLevel.PreciseBackground, context.locationPermissionLevel())
    }
}
