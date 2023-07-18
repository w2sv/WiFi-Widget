package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.widget.WidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.DialogButton
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.WidgetConfigurationViewModel
import kotlinx.coroutines.launch

@Composable
internal fun ButtonRow(
    modifier: Modifier = Modifier,
    widgetConfigurationViewModel: WidgetConfigurationViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val applyButtonEnabled by widgetConfigurationViewModel.nonAppliedWidgetConfiguration.stateChanged.collectAsState()

    ButtonRow(
        onCancel = {
            widgetConfigurationViewModel.onDismissWidgetConfigurationDialog()
        },
        onApply = {
            scope.launch {
                widgetConfigurationViewModel.nonAppliedWidgetConfiguration.sync()
                WidgetProvider.triggerDataRefresh(context)
                context.showToast(R.string.updated_widget_configuration)
                widgetConfigurationViewModel.showWidgetConfigurationDialog.value = false
            }
        },
        applyButtonEnabled = {
            applyButtonEnabled
        },
        modifier = modifier
    )
}

@Composable
private fun ButtonRow(
    onCancel: () -> Unit,
    onApply: () -> Unit,
    applyButtonEnabled: () -> Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DialogButton(onClick = onCancel) {
            JostText(text = stringResource(R.string.cancel))
        }
        DialogButton(onClick = onApply, enabled = applyButtonEnabled()) {
            JostText(text = stringResource(R.string.apply))
        }
    }
}