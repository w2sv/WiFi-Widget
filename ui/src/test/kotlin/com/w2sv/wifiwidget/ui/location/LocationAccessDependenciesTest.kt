package com.w2sv.wifiwidget.ui.location

import com.w2sv.domain.permission.LocationAccessRepository
import junit.framework.TestCase.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LocationAccessDependenciesTest {

    @Test
    fun `histories expose repository flows and persist callbacks`() =
        runTest {
            val requested = MutableStateFlow(false)
            val rationalShown = MutableStateFlow(false)
            val requestedWrites = mutableListOf<Boolean>()
            val rationalWrites = mutableListOf<Boolean>()
            val repository = object : LocationAccessRepository {
                override val permissionRequested: Flow<Boolean> = requested
                override val rationalShown: Flow<Boolean> = rationalShown

                override suspend fun recordPermissionRequested() {
                    requestedWrites += true
                }

                override suspend fun recordRationalShown() {
                    rationalWrites += true
                }
            }
            val dependencies = LocationAccessDependencies({ true }, repository, this)

            assertSame(requested, dependencies.requestHistory.wasRequestLaunchedBefore)
            assertSame(rationalShown, dependencies.rationalHistory.wasShownBefore)
            assertTrue(dependencies.isLocationEnabled())

            dependencies.requestHistory.recordRequestLaunched()
            dependencies.rationalHistory.recordWasShown()
            advanceUntilIdle()

            assertEquals(listOf(true), requestedWrites)
            assertEquals(listOf(true), rationalWrites)
        }
}
