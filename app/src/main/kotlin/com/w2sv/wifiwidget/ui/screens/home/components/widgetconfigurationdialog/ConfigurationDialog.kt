package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.widget.R
import com.w2sv.wifiwidget.ui.components.CustomDialog
import com.w2sv.wifiwidget.ui.components.DialogButtonRow
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.content.WidgetConfigurationDialogContent
import com.w2sv.wifiwidget.ui.viewmodels.HomeScreenViewModel
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel
import kotlinx.coroutines.launch

@Composable
fun WidgetConfigurationDialog(
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier,
    widgetVM: WidgetViewModel = viewModel(),
    homeScreenViewModel: HomeScreenViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val onDismissRequest: () -> Unit = {
        scope.launch {
            widgetVM.configuration.reset()
        }
        closeDialog()
    }

    CustomDialog(
        title = stringResource(id = com.w2sv.common.R.string.configure_widget),
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                painterResource(id = R.drawable.ic_settings_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = modifier
    ) {
        WidgetConfigurationDialogContent(
            widgetConfiguration = widgetVM.configuration,
            showInfoDialog = { widgetVM.infoDialogData.value = it },
            lapUIState = homeScreenViewModel.lapUIState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .heightIn(260.dp, 420.dp)
        )
        DialogButtonRow(
            onCancel = {
                onDismissRequest()
            },
            onApply = {
                widgetVM.syncConfiguration(context)
                closeDialog()
            },
            applyButtonEnabled = widgetVM.configuration.statesDissimilar.collectAsState().value,
            modifier = Modifier.fillMaxWidth()
        )
    }
}