package com.w2sv.wifiwidget.ui.location

import com.w2sv.domain.location.IsLocationEnabled
import com.w2sv.domain.location.LocationAccessRepository
import junit.framework.TestCase.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LocationAccessDependenciesTest {

    @Test
    fun `request history exposes repository state and records requests`() =
        runTest {
            val requested = MutableStateFlow(false)
            val onboardingCompleted = MutableStateFlow(false)
            val requestedWrites = mutableListOf<Boolean>()
            val repository = object : LocationAccessRepository {
                override val permissionRequested: Flow<Boolean> = requested
                override val onboardingCompleted: Flow<Boolean> = onboardingCompleted

                override suspend fun recordPermissionRequested() {
                    requestedWrites += true
                }

                override suspend fun completeOnboarding() {
                    onboardingCompleted.value = true
                }
            }
            val locationEnabled = object : IsLocationEnabled {
                override fun invoke(): Boolean =
                    true
                override val flow: Flow<Boolean> = flowOf(true)
            }
            val dependencies = LocationAccessDependencies(locationEnabled, repository, this)

            assertSame(requested, dependencies.requestHistory.wasRequestLaunchedBefore)
            assertTrue(dependencies.isLocationEnabled())

            dependencies.requestHistory.recordRequestLaunched()
            advanceUntilIdle()

            assertEquals(listOf(true), requestedWrites)
        }
}
