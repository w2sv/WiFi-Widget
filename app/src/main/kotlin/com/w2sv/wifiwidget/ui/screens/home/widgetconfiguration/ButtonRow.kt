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
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.widget.WidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.shared.DialogButton
import com.w2sv.wifiwidget.ui.shared.JostText
import kotlinx.coroutines.launch

@Composable
internal fun ButtonRow(
    modifier: Modifier = Modifier,
    widgetConfigurationViewModel: WidgetConfigurationViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val applyButtonEnabled by widgetConfigurationViewModel.widgetConfigurationStates.stateChanged.collectAsState()

    ButtonRow(
        onCancel = {
            widgetConfigurationViewModel.onDismissWidgetConfigurationDialog()
        },
        onApply = {
            scope.launch {
                widgetConfigurationViewModel.widgetConfigurationStates.sync()
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