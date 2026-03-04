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
import com.w2sv.composed.core.CollectFromFlow
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.LocalLocationAccessCapability
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.navigation.LocalNavigator
import com.w2sv.wifiwidget.ui.navigation.Navigator
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.sharedstate.location.access_capability.LocationAccessCapability
import com.w2sv.wifiwidget.ui.util.ScopedSnackbarEmitter
import com.w2sv.wifiwidget.ui.util.rememberScopedSnackbarEmitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update

@Composable
fun WidgetConfigScreenRoute(
    navigator: Navigator = LocalNavigator.current,
    locationAccessCapability: LocationAccessCapability = LocalLocationAccessCapability.current,
    viewModel: WidgetConfigScreenViewModel = hiltViewModel()
) {
    val onBack: () -> Unit = rememberOnBack(
        configIsDirty = { viewModel.reversibleConfig.isDirty.value },
        leaveScreen = navigator::leaveWidgetConfiguration,
        scope = rememberCoroutineScope()
    )

    val config by viewModel.reversibleConfig.collectAsStateWithLifecycle()
    val configIsDirty by viewModel.reversibleConfig.isDirty.collectAsStateWithLifecycle()

    var dialog by rememberSaveable { mutableStateOf<WidgetConfigDialog?>(null) }

    CollectFromFlow(locationAccessCapability.grantEvents) { event ->
        event.asEnabledPropertyOrNull?.run {
            viewModel.reversibleConfig.update {
                it.withConfiguredPropertyEnablement(
                    property = property,
                    isEnabled = true
                )
            }
        }
    }

    BackHandler(onBack = onBack)

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
        configIsDirty = configIsDirty,
        revertConfig = viewModel.reversibleConfig::revert,
        commitChanges = viewModel.reversibleConfig::launchCommit,
        showDialog = { dialog = it },
        onBackButtonClick = onBack,
        snackbarBuilderFlow = viewModel.snackbarBuilderFlow
    )
}

@Composable
private fun rememberOnBack(
    configIsDirty: () -> Boolean,
    leaveScreen: () -> Unit,
    scope: CoroutineScope = rememberCoroutineScope(),
    scopedSnackbarEmitter: ScopedSnackbarEmitter = rememberScopedSnackbarEmitter(scope = scope)
): () -> Unit {
    val backPressHandler = remember {
        BackPressHandler(
            coroutineScope = scope,
            confirmationWindowDuration = 2500L
        )
    }

    return remember(scopedSnackbarEmitter) {
        {
            onBack(
                configHasChanged = configIsDirty,
                backPressHandler = backPressHandler,
                scopedSnackbarEmitter = scopedSnackbarEmitter,
                leaveScreen = leaveScreen
            )
        }
    }
}

private fun onBack(
    configHasChanged: () -> Boolean,
    backPressHandler: BackPressHandler,
    scopedSnackbarEmitter: ScopedSnackbarEmitter,
    leaveScreen: () -> Unit
) {
    if (configHasChanged()) {
        backPressHandler(
            onFirstPress = {
                scopedSnackbarEmitter.dismissCurrentAndShow {
                    AppSnackbarVisuals(
                        msg = getString(R.string.go_back_on_unsaved_changes_warning),
                        kind = SnackbarKind.Warning
                    )
                }
            },
            onSecondPress = {
                scopedSnackbarEmitter.dismissCurrent()
                leaveScreen()
            }
        )
    } else {
        scopedSnackbarEmitter.dismissCurrent()
        leaveScreen()
    }
}
