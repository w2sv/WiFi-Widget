package com.w2sv.wifiwidget.ui.screen.widgetconfig

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.androidutils.BackPressHandler
import com.w2sv.composed.material3.rememberSnackbarLauncher
import com.w2sv.composed.material3.replaceCurrentWith
import com.w2sv.composed.runtime.CollectFromFlow
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.location.capability.access.LocalLocationAccessCapability
import com.w2sv.wifiwidget.ui.location.capability.access.LocationAccessCapability
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

const val NAVIGATE_BACK_CONFIRMATION_WINDOW = 2500L

@Composable
fun WidgetConfigRoute(
    navigateBack: () -> Unit,
    locationAccessCapability: LocationAccessCapability = LocalLocationAccessCapability.current,
    viewModel: WidgetConfigScreenViewModel = hiltViewModel()
) {
    val onBack = rememberWidgetConfigBackHandler(
        configIsDirty = viewModel.reversibleConfig.isDirty,
        navigateBack = navigateBack
    )

    val config by viewModel.reversibleConfig.collectAsStateWithLifecycle()
    val configEditState = rememberConfigEditState(viewModel)

    var dialog by rememberSaveable { mutableStateOf<WidgetConfigDialog?>(null) }

    // Perform pending property update if required location access has been granted
    CollectFromFlow(locationAccessCapability.grantEvents) { event ->
        event.asEnablePropertyOrNull?.run {
            viewModel.reversibleConfig.update {
                it.withUpdatedPropertyEnablement(
                    property = property,
                    isEnabled = true
                )
            }
        }
    }

    dialog?.let {
        WidgetConfigDialog(
            dialog = it,
            updateDialog = { updatedDialog -> dialog = updatedDialog },
            updateConfig = viewModel.reversibleConfig::update,
            onDismissRequest = { dialog = null }
        )
    }

    WidgetConfigScreen(
        config = config,
        updateConfig = viewModel.reversibleConfig::update,
        configEditState = configEditState,
        showDialog = { dialog = it },
        onBackButtonClick = onBack
    )
}

/**
 * Intercepts system back while the configuration is dirty and returns the same back action for the toolbar.
 * A dirty configuration requires a second back action within [NAVIGATE_BACK_CONFIRMATION_WINDOW] before [navigateBack] is called.
 */
@Composable
internal fun rememberWidgetConfigBackHandler(configIsDirty: StateFlow<Boolean>, navigateBack: () -> Unit): () -> Unit {
    val isDirty by configIsDirty.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarLauncher = rememberSnackbarLauncher(scope = scope, snackbarHostState = LocalSnackbarHostState.current)
    val backPressHandler = remember(scope, isDirty) {
        BackPressHandler(
            coroutineScope = scope,
            confirmationWindowDuration = NAVIGATE_BACK_CONFIRMATION_WINDOW
        )
    }

    val leaveScreen = {
        snackbarLauncher.dismissCurrent()
        navigateBack()
    }
    val onBack: () -> Unit = {
        if (configIsDirty.value) {
            backPressHandler(
                onFirstPress = {
                    snackbarLauncher.replaceCurrentWith {
                        AppSnackbarVisuals(
                            msg = getString(R.string.go_back_on_unsaved_changes_warning),
                            kind = SnackbarKind.Warning
                        )
                    }
                },
                onSecondPress = leaveScreen
            )
        } else {
            leaveScreen()
        }
    }

    // Disable handler on clean config for predictive back gesture to work
    BackHandler(enabled = isDirty, onBack = onBack)

    return onBack
}
