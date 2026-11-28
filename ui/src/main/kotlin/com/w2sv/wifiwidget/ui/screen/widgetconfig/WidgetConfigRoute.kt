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
import com.w2sv.androidutils.backpress.BackPressHandler
import com.w2sv.composed.material3.rememberSnackbarLauncher
import com.w2sv.composed.material3.replaceCurrentWith
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import kotlinx.coroutines.flow.StateFlow

private const val NAVIGATE_BACK_CONFIRMATION_WINDOW = 2500L

@Composable
fun WidgetConfigRoute(
    onExit: () -> Unit,
    isLauncherConfiguration: Boolean = false,
    viewModel: WidgetConfigScreenViewModel = hiltViewModel()
) {
    val onBack = rememberWidgetConfigBackHandler(
        configIsDirty = viewModel.reversibleConfig.isDirty,
        onExit = onExit,
        interceptCleanBack = isLauncherConfiguration
    )

    val config by viewModel.reversibleConfig.collectAsStateWithLifecycle()
    val configEditState = rememberConfigEditState(viewModel)

    var dialog by rememberSaveable { mutableStateOf<WidgetConfigDialog?>(null) }

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
        onBackButtonClick = onBack,
        showCloseIcon = isLauncherConfiguration
    )
}

/**
 * Intercepts system Back while dirty, and also while clean for launcher configuration.
 * A dirty configuration requires a second back action within [NAVIGATE_BACK_CONFIRMATION_WINDOW] before [onExit] is called.
 * Returns the same action for the toolbar so both entry points share confirmation behavior.
 */
@Composable
internal fun rememberWidgetConfigBackHandler(
    configIsDirty: StateFlow<Boolean>,
    onExit: () -> Unit,
    interceptCleanBack: Boolean = false
): () -> Unit {
    val isDirty by configIsDirty.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarLauncher = rememberSnackbarLauncher(scope = scope, snackbarHostState = LocalSnackbarHostState.current)
    val backPressHandler = remember(scope, isDirty) {
        BackPressHandler(
            scope = scope,
            confirmationWindowMillis = NAVIGATE_BACK_CONFIRMATION_WINDOW
        )
    }

    val leaveScreen = {
        snackbarLauncher.dismissCurrent()
        onExit()
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

    // Launcher Back must set RESULT_OK even when the configuration is clean.
    BackHandler(enabled = isDirty || interceptCleanBack, onBack = onBack)

    return onBack
}
