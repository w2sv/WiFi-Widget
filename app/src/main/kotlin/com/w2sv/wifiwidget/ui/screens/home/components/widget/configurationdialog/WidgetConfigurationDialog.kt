package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.common.R
import com.w2sv.wifiwidget.ui.components.CancelApplyButtonRow
import com.w2sv.wifiwidget.ui.components.DialogHeader
import com.w2sv.wifiwidget.ui.components.ElevatedCardDialog
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.PropertyInfoDialog
import com.w2sv.wifiwidget.ui.utils.conditional
import com.w2sv.wifiwidget.ui.utils.isLandscapeModeActivated
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun WidgetConfigurationDialog(
    locationAccessState: LocationAccessState,
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier,
    widgetVM: WidgetViewModel = viewModel(),
    scope: CoroutineScope = rememberCoroutineScope(),
) {
    val onDismissRequest: () -> Unit = remember {
        {
            scope.launch {
                widgetVM.configuration.reset()
            }
            closeDialog()
        }
    }

    ElevatedCardDialog(
        header = DialogHeader(
            title = stringResource(id = R.string.configure_widget),
            icon = {
                Icon(
                    painterResource(id = com.w2sv.widget.R.drawable.ic_settings_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
        ),
        onDismissRequest = onDismissRequest,
        modifier = modifier.conditional(
            condition = isLandscapeModeActivated,
            onTrue = { fillMaxHeight() }
        ),
    ) {
        WidgetConfigurationDialogContent(
            widgetConfiguration = widgetVM.configuration,
            locationAccessState = locationAccessState,
            showPropertyInfoDialog = widgetVM::setPropertyInfoDialogData,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f),
        )
        widgetVM.propertyInfoDialogData.collectAsStateWithLifecycle().value?.let {
            PropertyInfoDialog(
                data = it,
                onDismissRequest = { widgetVM.setPropertyInfoDialogData(null) })
        }
        CancelApplyButtonRow(
            onCancel = {
                onDismissRequest()
            },
            onApply = {
                widgetVM.configuration.launchSync()
                closeDialog()
            },
            applyButtonEnabled = widgetVM.configuration.statesDissimilar.collectAsStateWithLifecycle().value,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
