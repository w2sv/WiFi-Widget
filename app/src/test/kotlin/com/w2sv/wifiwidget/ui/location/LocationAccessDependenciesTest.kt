package com.w2sv.wifiwidget.ui.location

import com.w2sv.datastoreutils.datastoreflow.DataStoreFlow
import com.w2sv.domain.repository.PermissionRepository
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertSame
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
            val requestedStore = DataStoreFlow(requested, { false }) { requestedWrites += it }
            val rationalStore = DataStoreFlow(rationalShown, { false }) { rationalWrites += it }
            val repository = mockk<PermissionRepository> {
                every { locationAccessPermissionRequested } returns requestedStore
                every { locationAccessPermissionRationalShown } returns rationalStore
            }
            val dependencies = LocationAccessDependencies({ true }, repository, this)

            assertSame(requestedStore, dependencies.requestHistory.wasRequestLaunchedBefore)
            assertSame(rationalStore, dependencies.rationalHistory.wasShownBefore)
            assertTrue(dependencies.isLocationEnabled())

            dependencies.requestHistory.recordRequestLaunched()
            dependencies.rationalHistory.recordWasShown()
            advanceUntilIdle()

            assertEquals(listOf(true), requestedWrites)
            assertEquals(listOf(true), rationalWrites)
        }
}
