package com.w2sv.wifiwidget.ui.screen.locationaccess.components

import androidx.annotation.StringRes
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.screen.locationaccess.state.LocationAccessScreenState

/**
 * Immutable panel content derived from screen inputs. Status follows actual availability, while
 * actions follow the retained displayed permission level. Callbacks and animation history stay
 * with the composables.
 */
internal data class LocationAccessActionPanelPresentation(
    val status: LocationAccessPresentationStatus?,
    val primaryAction: Action,
    val secondaryAction: Action?,
    val showAppSettingsAction: Boolean,
    val showOnboardingHint: Boolean,
    val actionsEnabled: Boolean
) {
    data class Action(@StringRes val labelRes: Int, val kind: ActionKind)

    enum class ActionKind {
        RequestForeground,
        RequestBackground,
        Continue,
        OpenAppSettings
    }

    companion object {
        fun from(state: LocationAccessScreenState): LocationAccessActionPanelPresentation {
            val continueAction = Action(
                if (state.isOnboarding) R.string.continue_to_wifi_widget else R.string.done,
                Continue
            )
            return LocationAccessActionPanelPresentation(
                status = LocationAccessPresentationStatus.from(state.availability),
                primaryAction = primaryAction(state, continueAction),
                secondaryAction = secondaryAction(state, continueAction),
                showAppSettingsAction = !state.isOnboarding && state.displayedPermissionLevel.isPreciseForeground,
                showOnboardingHint = state.isOnboarding,
                actionsEnabled = state.actionsEnabled
            )
        }

        private fun primaryAction(state: LocationAccessScreenState, continueAction: Action): Action =
            when (state.displayedPermissionLevel) {
                None, ApproximateOnly -> Action(
                    if (state.foregroundRequestOpensSettings) {
                        R.string.go_to_app_settings
                    } else {
                        R.string.enable_location_access
                    },
                    RequestForeground
                )
                PreciseForeground -> Action(R.string.enable_background_updates, RequestBackground)
                PreciseBackground -> continueAction
            }

        private fun secondaryAction(state: LocationAccessScreenState, continueAction: Action): Action? =
            when (state.displayedPermissionLevel) {
                None, ApproximateOnly ->
                    if (state.isOnboarding) {
                        Action(
                            R.string.continue_without_location_access,
                            Continue
                        )
                    } else {
                        null
                    }
                PreciseForeground -> continueAction
                PreciseBackground ->
                    if (state.isOnboarding) {
                        null
                    } else {
                        Action(
                            R.string.manage_in_android_settings,
                            OpenAppSettings
                        )
                    }
            }
    }
}
