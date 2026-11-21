package com.w2sv.wifiwidget.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.composed.material3.rememberSnackbarController
import com.w2sv.composed.runtime.CollectFromFlow
import com.w2sv.composed.runtime.CollectLatestFromFlow
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.SnackbarAction
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.designsystem.theme.ThemeController
import com.w2sv.wifiwidget.ui.location.capability.access.LocalLocationAccessCapability
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessScreenScenario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import slimber.log.i

@Composable
fun HomeRoute(
    themeController: ThemeController,
    toWidgetConfiguration: () -> Unit,
    toLocationAccess: (LocationAccessScreenScenario) -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    ObserveLocationAccessChanges(onAccessChange = viewModel::onLocationAccessChanged)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onForeground()
        viewModel.onLocationAccessChanged()
    }

    ShowSnackbarOnWidgetPin(
        widgetPinSuccessFlow = viewModel.widgetPinSuccessFlow,
        anyLocationAccessRequiringPropertyEnabled = { viewModel.isAnyLocationAccessRequiringPropertyEnabled },
        toLocationAccess = toLocationAccess
    )

    val wifiState by viewModel.wifiState.collectAsStateWithLifecycle()

    HomeScreen(
        themeController = themeController,
        wifiState = wifiState,
        pinWidget = viewModel::pinWidget,
        toWidgetConfiguration = toWidgetConfiguration,
        toLocationAccess = { toLocationAccess(ManagePermissions) },
        snackbarBuilderFlow = viewModel.snackbarBuilder
    )
}

@Composable
private fun ObserveLocationAccessChanges(onAccessChange: () -> Unit) {
    val locationAccessCapability = LocalLocationAccessCapability.current

    val locationAccessStatusChanges = remember(locationAccessCapability) {
        snapshotFlow { locationAccessCapability.permissionLevel.hasPreciseAccess }.drop(1)
    }

    CollectFromFlow(locationAccessStatusChanges) {
        i { "Calling onLocationAccessChanged on new location access status $it" }
        onAccessChange()
    }
}

@Composable
private fun ShowSnackbarOnWidgetPin(
    widgetPinSuccessFlow: Flow<Unit>,
    anyLocationAccessRequiringPropertyEnabled: () -> Boolean,
    toLocationAccess: (LocationAccessScreenScenario) -> Unit
) {
    val snackbarController = rememberSnackbarController(LocalSnackbarHostState.current)
    val locationAccess = LocalLocationAccessCapability.current

    CollectLatestFromFlow(widgetPinSuccessFlow) {
        snackbarController.replaceCurrentWith {
            when {
                // Warn when system location services are disabled.
                anyLocationAccessRequiringPropertyEnabled() && !locationAccess.locationServicesEnabled -> AppSnackbarVisuals(
                    msg = getString(R.string.on_pin_widget_wo_gps_enabled),
                    kind = SnackbarKind.Warning,
                    action = SnackbarAction(
                        label = getString(R.string.enable),
                        callback = locationAccess::openLocationSettings
                    )
                )

                anyLocationAccessRequiringPropertyEnabled() && !locationAccess.permissionLevel.hasPreciseAccess -> AppSnackbarVisuals(
                    msg = getString(R.string.on_pin_widget_wo_location_access_permission),
                    kind = SnackbarKind.Warning,
                    action = SnackbarAction(
                        label = getString(R.string.grant),
                        callback = { toLocationAccess(ManagePermissions) }
                    )
                )

                // Warn about (B)SSID not being reliably displayed if background location access not granted
                anyLocationAccessRequiringPropertyEnabled() &&
                    locationAccess.permissionLevel.isPreciseForeground -> AppSnackbarVisuals(
                    msg = getString(R.string.on_pin_widget_wo_background_location_access_permission),
                    kind = SnackbarKind.Warning,
                    action = SnackbarAction(
                        label = getString(R.string.grant),
                        callback = { toLocationAccess(ManagePermissions) }
                    )
                )

                else -> AppSnackbarVisuals(
                    msg = getString(R.string.pinned_widget),
                    kind = SnackbarKind.Success
                )
            }
        }
    }
}
