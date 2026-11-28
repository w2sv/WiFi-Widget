package com.w2sv.wifiwidget.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.composed.material3.rememberSnackbarController
import com.w2sv.composed.runtime.CollectLatestFromFlow
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.designsystem.theme.ThemeController
import com.w2sv.wifiwidget.ui.location.capability.access.LocalLocationAccessCapability
import kotlinx.coroutines.flow.Flow

@Composable
fun HomeRoute(
    themeController: ThemeController,
    toWidgetConfiguration: () -> Unit,
    toLocationAccess: () -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    ObservePreciseLocationAccess(onAccessChange = viewModel::onLocationAccessChanged)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME, onEvent = viewModel::onResume)

    ConfirmationSnackbarOnWidgetPin(widgetPinSuccessFlow = viewModel.widgetPinSuccessFlow)

    val wifiState by viewModel.wifiState.collectAsStateWithLifecycle()

    HomeScreen(
        themeController = themeController,
        wifiState = wifiState,
        pinWidget = viewModel::pinWidget,
        toWidgetConfiguration = toWidgetConfiguration,
        toLocationAccess = toLocationAccess,
        snackbarBuilderFlow = viewModel.snackbarBuilder
    )
}

@Composable
private fun ObservePreciseLocationAccess(onAccessChange: () -> Unit) {
    val hasPreciseAccess = LocalLocationAccessCapability.current.permissionLevel.hasPreciseAccess
    val currentOnAccessChange by rememberUpdatedState(onAccessChange)

    LaunchedEffect(hasPreciseAccess) {
        currentOnAccessChange()
    }
}

@Composable
private fun ConfirmationSnackbarOnWidgetPin(widgetPinSuccessFlow: Flow<Unit>) {
    val snackbarController = rememberSnackbarController(LocalSnackbarHostState.current)

    CollectLatestFromFlow(widgetPinSuccessFlow) {
        snackbarController.replaceCurrentWith {
            AppSnackbarVisuals(
                msg = getString(R.string.pinned_widget),
                kind = SnackbarKind.Success
            )
        }
    }
}
