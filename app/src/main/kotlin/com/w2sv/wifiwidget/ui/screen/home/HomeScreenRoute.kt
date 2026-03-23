package com.w2sv.wifiwidget.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.composed.core.CollectFromFlow
import com.w2sv.composed.core.CollectLatestFromFlow
import com.w2sv.core.common.R
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.wifiwidget.ui.LocalLocationAccessCapability
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarAction
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.sharedstate.location.OnLocationAccessGranted.EnableLocationAccessRequiringProperties
import com.w2sv.wifiwidget.ui.sharedstate.location.OnLocationAccessGranted.TriggerWidgetDataRefresh
import com.w2sv.wifiwidget.ui.sharedstate.theme.rememberThemeController
import com.w2sv.wifiwidget.ui.util.snackbar.rememberSnackbarController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import slimber.log.i

@Composable
fun HomeScreenRoute(viewModel: HomeScreenViewModel = hiltViewModel()) {
    BindLocationAccessToViewModel()

    ShowSnackbarOnWidgetPin(
        widgetPinSuccessFlow = viewModel.widgetPinSuccessFlow,
        anyLocationAccessRequiringPropertyEnabled = { viewModel.isAnyLocationAccessRequiringPropertyEnabled }
    )

    val themeController = rememberThemeController()
    val wifiState by viewModel.wifiState.collectAsStateWithLifecycle()

    HomeScreen(
        themeController = themeController,
        wifiState = wifiState,
        pinWidget = viewModel::pinWidget,
        snackbarBuilderFlow = viewModel.snackbarBuilder
    )
}

@Composable
private fun BindLocationAccessToViewModel(viewModel: HomeScreenViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val locationAccessCapability = LocalLocationAccessCapability.current

    val locationAccessStatusChanges = remember(locationAccessCapability) {
        snapshotFlow { locationAccessCapability.foregroundPermissionsGranted }.drop(1)
    }

    // Call viewModel.onLocationAccessChanged on locationAccessStatusChanges emissions
    CollectFromFlow(locationAccessStatusChanges) {
        i { "Calling onLocationAccessChanged on new location access status $it" }
        viewModel.onLocationAccessChanged()
    }

    // Consume OnLocationAccessGranted events relevant for the screen
    CollectFromFlow(locationAccessCapability.grantEvents) { event ->
        when (event) {
            TriggerWidgetDataRefresh -> WifiWidgetProvider.refreshData(context)
            EnableLocationAccessRequiringProperties -> viewModel.enableLocationAccessRequiringProperties()
            else -> Unit
        }
    }
}

@Composable
private fun ShowSnackbarOnWidgetPin(widgetPinSuccessFlow: Flow<Unit>, anyLocationAccessRequiringPropertyEnabled: () -> Boolean) {
    val snackbarController = rememberSnackbarController()
    val locationAccess = LocalLocationAccessCapability.current

    CollectLatestFromFlow(widgetPinSuccessFlow) {
        snackbarController.showReplacing {
            when {
                // Warn about (B)SSID not being displayed if device GPS is disabled
                anyLocationAccessRequiringPropertyEnabled() && !locationAccess.isGpsEnabled -> AppSnackbarVisuals(
                    msg = getString(R.string.on_pin_widget_wo_gps_enabled),
                    kind = SnackbarKind.Warning,
                    action = SnackbarAction(
                        label = getString(R.string.enable),
                        callback = locationAccess::openLocationSettings
                    )
                )

                anyLocationAccessRequiringPropertyEnabled() && !locationAccess.foregroundPermissionsGranted -> AppSnackbarVisuals(
                    msg = getString(R.string.on_pin_widget_wo_location_access_permission),
                    kind = SnackbarKind.Warning,
                    action = SnackbarAction(
                        label = getString(R.string.grant),
                        callback = { locationAccess.requestPermission(onGrant = TriggerWidgetDataRefresh) }
                    )
                )

                // Warn about (B)SSID not being reliably displayed if background location access not granted
                anyLocationAccessRequiringPropertyEnabled() && locationAccess.isBackgroundPermissionMissing -> AppSnackbarVisuals(
                    msg = getString(R.string.on_pin_widget_wo_background_location_access_permission),
                    kind = SnackbarKind.Warning,
                    action = SnackbarAction(
                        label = getString(R.string.grant),
                        callback = locationAccess::launchBackgroundPermission
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
