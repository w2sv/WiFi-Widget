package com.w2sv.wifiwidget.ui.screen.locationaccess

import androidx.lifecycle.SavedStateHandle
import com.w2sv.domain.location.LocationAccessRepository
import com.w2sv.domain.location.LocationPermissionLevel
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigDataSource
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessScreenScenario.ManageAccess
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessScreenScenario.Onboarding
import com.w2sv.wifiwidget.ui.screen.locationaccess.flow.LocationAccessEvent
import com.w2sv.wifiwidget.ui.screen.locationaccess.flow.LocationAccessFlowState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LocationAccessScreenViewModelTest {
    private var storedConfig = WidgetConfig.default.run {
        WifiProperty.locationAccessRequiring.fold(this) { config, property ->
            config.withUpdatedPropertyEnablement(property, false)
        }
    }
    private val dataSource = mockk<WidgetConfigDataSource> {
        coEvery { update(any()) } coAnswers {
            storedConfig = firstArg<(WidgetConfig) -> WidgetConfig>()(storedConfig)
        }
    }
    private val repository = mockk<LocationAccessRepository>(relaxed = true)

    @Test
    fun `onboarding grant offers background access then completion enables default properties`() =
        runTest {
            val viewModel = viewModel(Onboarding)
            val original = storedConfig
            viewModel.onEvent(LocationAccessEvent.RequestForeground(false, LocationPermissionLevel.None))

            viewModel.onEvent(LocationAccessEvent.ForegroundAccessAvailable(LocationPermissionLevel.PreciseForeground))
            assertEquals(
                LocationAccessFlowState.BackgroundDisclosure(LocationPermissionLevel.PreciseForeground),
                viewModel.flowState.value
            )
            assertEquals(original, storedConfig)
            viewModel.onEvent(LocationAccessEvent.DismissBackgroundDisclosure)
            assertEquals(
                LocationAccessFlowState.Completing(LocationPermissionLevel.PreciseForeground),
                viewModel.flowState.value
            )
            viewModel.completeOnboarding(hasPreciseAccess = true)

            assertEquals(original.withEnabledLocationAccessRequiringProperties(), storedConfig)
            coVerify(exactly = 1) { repository.completeOnboarding() }
        }

    @Test
    fun `denial and onboarding completion without access leave properties disabled`() =
        runTest {
            val viewModel = viewModel(Onboarding)
            val original = storedConfig
            viewModel.onEvent(LocationAccessEvent.RequestForeground(false, LocationPermissionLevel.None))

            viewModel.onEvent(LocationAccessEvent.ForegroundDenied)
            assertNull(viewModel.flowState.value)
            viewModel.completeOnboarding(hasPreciseAccess = false)

            assertEquals(original, storedConfig)
            coVerify(exactly = 0) { dataSource.update(any()) }
            coVerify(exactly = 1) { repository.completeOnboarding() }
        }

    @Test
    fun `restored foreground Settings interaction accepts the result once without configuration side effects`() {
        val savedStateHandle = SavedStateHandle()
        viewModel(ManageAccess, savedStateHandle).apply {
            onEvent(LocationAccessEvent.RequestForeground(true, LocationPermissionLevel.None))
            onEvent(LocationAccessEvent.LeavingForSettings)
        }
        val restored = viewModel(ManageAccess, savedStateHandle)
        assertEquals(
            LocationAccessFlowState.ForegroundSettings(LocationPermissionLevel.None, hasLeftApp = true),
            restored.flowState.value
        )

        restored.onEvent(LocationAccessEvent.ForegroundAccessAvailable(LocationPermissionLevel.PreciseForeground))
        restored.onEvent(LocationAccessEvent.ForegroundAccessAvailable(LocationPermissionLevel.PreciseForeground))

        assertNull(restored.flowState.value)
        coVerify(exactly = 0) { dataSource.update(any()) }
    }

    @Test
    fun `restored background Settings interaction accepts the result once without configuration side effects`() {
        val savedStateHandle = SavedStateHandle()
        viewModel(ManageAccess, savedStateHandle).apply {
            onEvent(LocationAccessEvent.ShowBackgroundDisclosure(LocationPermissionLevel.PreciseForeground))
            onEvent(LocationAccessEvent.RequestBackground)
            onEvent(LocationAccessEvent.LeavingForSettings)
        }
        val restored = viewModel(ManageAccess, savedStateHandle)
        assertEquals(
            LocationAccessFlowState.BackgroundSettings(LocationPermissionLevel.PreciseForeground, hasLeftApp = true),
            restored.flowState.value
        )

        restored.onEvent(LocationAccessEvent.BackgroundAccessAvailable)
        restored.onEvent(LocationAccessEvent.BackgroundAccessAvailable)

        assertNull(restored.flowState.value)
        coVerify(exactly = 0) { dataSource.update(any()) }
    }

    @Test
    fun `standalone foreground and background grants keep flow open without onboarding or configuration side effects`() {
        val viewModel = viewModel(ManageAccess)
        viewModel.onEvent(LocationAccessEvent.RequestForeground(false, LocationPermissionLevel.None))
        viewModel.onEvent(LocationAccessEvent.ForegroundAccessAvailable(LocationPermissionLevel.PreciseForeground))

        // Keep the route open so the existing optional background-access action remains available.
        assertNull(viewModel.flowState.value)
        viewModel.onEvent(LocationAccessEvent.ShowBackgroundDisclosure(LocationPermissionLevel.PreciseForeground))
        viewModel.onEvent(LocationAccessEvent.RequestBackground)
        viewModel.onEvent(LocationAccessEvent.LeavingForSettings)
        viewModel.onEvent(LocationAccessEvent.BackgroundAccessAvailable)

        assertNull(viewModel.flowState.value)
        coVerify(exactly = 0) { repository.completeOnboarding() }
        coVerify(exactly = 0) { dataSource.update(any()) }
    }

    private fun viewModel(scenario: LocationAccessScreenScenario, savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        LocationAccessScreenViewModel(dataSource, repository, savedStateHandle, scenario)
}
