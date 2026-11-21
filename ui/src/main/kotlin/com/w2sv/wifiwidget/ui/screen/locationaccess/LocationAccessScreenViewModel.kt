package com.w2sv.wifiwidget.ui.screen.locationaccess

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.w2sv.domain.location.LocationAccessRepository
import com.w2sv.domain.widget.WidgetConfigDataSource
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.wifiwidget.ui.location.LocationPermissionLevel
import com.w2sv.wifiwidget.ui.screen.locationaccess.flow.LocationAccessEffect
import com.w2sv.wifiwidget.ui.screen.locationaccess.flow.LocationAccessEvent
import com.w2sv.wifiwidget.ui.screen.locationaccess.flow.LocationAccessFlowState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel(assistedFactory = LocationAccessScreenViewModel.Factory::class)
class LocationAccessScreenViewModel @AssistedInject constructor(
    private val configDataSource: WidgetConfigDataSource,
    private val locationAccessRepository: LocationAccessRepository,
    private val savedStateHandle: SavedStateHandle,
    @Assisted private val scenario: LocationAccessScreenScenario
) : ViewModel() {

    internal val flowState: StateFlow<LocationAccessFlowState?>
        field = MutableStateFlow(
            // Only the pending Settings kind is saved, so restoration uses its original action-level fallback.
            when (savedStateHandle.get<String>(PENDING_SETTINGS_STAGE)) {
                PENDING_FOREGROUND_SETTINGS -> LocationAccessFlowState.ForegroundSettings(
                    LocationPermissionLevel.None,
                    hasLeftApp = true
                )

                PENDING_BACKGROUND_SETTINGS -> LocationAccessFlowState.BackgroundSettings(
                    LocationPermissionLevel.PreciseForeground,
                    hasLeftApp = true
                )

                else -> null
            }
        )

    internal fun onEvent(event: LocationAccessEvent): LocationAccessEffect? {
        when (event) {
            is RequestForeground -> return requestForeground(event)

            is ForegroundAccessAvailable -> onForegroundAccessAvailable(event.permissionLevel)

            is ForegroundDenied if (foregroundResultLevel() != null) -> moveTo(null)

            is ShowBackgroundDisclosure -> transitionFromIdle(LocationAccessFlowState.BackgroundDisclosure(event.displayedPermissionLevel))

            is RequestBackground -> return requestBackground(event)

            is DismissBackgroundDisclosure -> onDismissBackgroundDisclosure()

            is BackgroundAccessAvailable -> onBackgroundAccessAvailable()

            is BackgroundDenied if (backgroundResultLevel() != null) -> moveTo(null)

            is LeavingForSettings -> onLeavingForSettings()

            is Complete -> transitionFromIdle(LocationAccessFlowState.Completing(event.displayedPermissionLevel))

            else -> Unit
        }
        return null
    }

    suspend fun completeOnboarding(hasPreciseAccess: Boolean) {
        if (hasPreciseAccess) {
            ensureDefaultPropertiesEnabled()
        }
        locationAccessRepository.completeOnboarding()
    }

    private fun requestForeground(event: LocationAccessEvent.RequestForeground): LocationAccessEffect? {
        val next = if (event.opensSettings) {
            LocationAccessFlowState.ForegroundSettings(event.displayedPermissionLevel)
        } else {
            LocationAccessFlowState.RequestingForeground(event.displayedPermissionLevel)
        }
        if (!transitionFromIdle(next)) return null
        return if (event.opensSettings) LocationAccessEffect.OpenForegroundSettings else LocationAccessEffect.RequestForegroundPermission
    }

    private fun requestBackground(event: LocationAccessEvent.RequestBackground): LocationAccessEffect? {
        val disclosure = flowState.value as? LocationAccessFlowState.BackgroundDisclosure ?: return null
        val next = if (event.opensSettings) {
            LocationAccessFlowState.BackgroundSettings(disclosure.displayedPermissionLevel)
        } else {
            LocationAccessFlowState.RequestingBackground(disclosure.displayedPermissionLevel)
        }
        moveTo(next)
        return LocationAccessEffect.LaunchBackgroundPermission
    }

    private fun onForegroundAccessAvailable(permissionLevel: LocationPermissionLevel) {
        val displayedPermissionLevel = foregroundResultLevel() ?: return
        if (
            permissionLevel != LocationPermissionLevel.PreciseForeground &&
            permissionLevel != LocationPermissionLevel.PreciseBackground
        ) {
            return
        }
        moveTo(
            when {
                scenario != Onboarding -> null
                permissionLevel.isPreciseForeground -> LocationAccessFlowState.BackgroundDisclosure(permissionLevel)
                else -> LocationAccessFlowState.Completing(displayedPermissionLevel)
            }
        )
    }

    private fun onDismissBackgroundDisclosure() {
        val current = flowState.value
        if (current !is LocationAccessFlowState.BackgroundDisclosure) return
        val next = LocationAccessFlowState.Completing(current.displayedPermissionLevel).takeIf { scenario == Onboarding }
        moveTo(next)
    }

    private fun onBackgroundAccessAvailable() {
        val displayedPermissionLevel = backgroundResultLevel() ?: return
        moveTo(LocationAccessFlowState.Completing(displayedPermissionLevel).takeIf { scenario == Onboarding })
    }

    private fun onLeavingForSettings() {
        when (val current = flowState.value) {
            is ForegroundSettings -> if (!current.hasLeftApp) moveTo(current.copy(hasLeftApp = true))
            is BackgroundSettings -> if (!current.hasLeftApp) moveTo(current.copy(hasLeftApp = true))
            else -> Unit
        }
    }

    private fun foregroundResultLevel(): LocationPermissionLevel? =
        when (val current = flowState.value) {
            is RequestingForeground -> current.displayedPermissionLevel
            is ForegroundSettings -> current.displayedPermissionLevel.takeIf { current.hasLeftApp }
            else -> null
        }

    private fun backgroundResultLevel(): LocationPermissionLevel? =
        when (val current = flowState.value) {
            is RequestingBackground -> current.displayedPermissionLevel
            is BackgroundSettings -> current.displayedPermissionLevel.takeIf { current.hasLeftApp }
            else -> null
        }

    private fun transitionFromIdle(next: LocationAccessFlowState): Boolean {
        if (flowState.value != null) return false
        moveTo(next)
        return true
    }

    private fun moveTo(next: LocationAccessFlowState?) {
        flowState.value = next
        when (next) {
            is ForegroundSettings if next.hasLeftApp -> savedStateHandle[PENDING_SETTINGS_STAGE] = PENDING_FOREGROUND_SETTINGS
            is BackgroundSettings if next.hasLeftApp -> savedStateHandle[PENDING_SETTINGS_STAGE] = PENDING_BACKGROUND_SETTINGS
            else -> savedStateHandle.remove<String>(PENDING_SETTINGS_STAGE)
        }
    }

    private suspend fun ensureDefaultPropertiesEnabled() {
        configDataSource.update { current ->
            if (WifiProperty.locationAccessRequiring.all(current::isEnabled)) {
                current
            } else {
                current.withEnabledLocationAccessRequiringProperties()
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(scenario: LocationAccessScreenScenario): LocationAccessScreenViewModel
    }

    private companion object {
        const val PENDING_SETTINGS_STAGE = "pending_location_settings_stage"
        const val PENDING_FOREGROUND_SETTINGS = "ForegroundSettingsAway"
        const val PENDING_BACKGROUND_SETTINGS = "BackgroundSettingsAway"
    }
}
