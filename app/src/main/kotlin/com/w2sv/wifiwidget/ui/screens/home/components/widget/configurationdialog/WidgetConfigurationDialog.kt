package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.common.R
import com.w2sv.wifiwidget.ui.components.CancelApplyButtonRow
import com.w2sv.wifiwidget.ui.components.DialogHeader
import com.w2sv.wifiwidget.ui.components.ElevatedCardDialog
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.PropertyInfoDialog
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.InfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.UnconfirmedWidgetConfiguration
import com.w2sv.wifiwidget.ui.utils.isLandscapeModeActivated
import com.w2sv.wifiwidget.ui.utils.thenIf

private const val infoDialogDataRememberKey = "WIDGET_CONFIGURATION_DIALOG_INFO_DIALOG_DATA"

@Composable
fun WidgetConfigurationDialog(
    locationAccessState: LocationAccessState,
    widgetConfiguration: UnconfirmedWidgetConfiguration,
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resetConfigAndCloseDialog: () -> Unit = remember {
        {
            widgetConfiguration.reset()
            closeDialog()
        }
    }

    var infoDialogData by rememberSaveable(
        stateSaver = InfoDialogData.nullableStateSaver,
        key = infoDialogDataRememberKey
    ) {
        mutableStateOf(null)
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
        onDismissRequest = resetConfigAndCloseDialog,
        modifier = modifier.thenIf(
            condition = isLandscapeModeActivated,
            onTrue = { fillMaxHeight() }
        ),
    ) {
        WidgetConfigurationDialogContent(
            widgetConfiguration = widgetConfiguration,
            locationAccessState = locationAccessState,
            showPropertyInfoDialog = { infoDialogData = it },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f),
        )
        CancelApplyButtonRow(
            onCancel = {
                resetConfigAndCloseDialog()
            },
            onApply = {
                widgetConfiguration.launchSync()
                closeDialog()
            },
            applyButtonEnabled = widgetConfiguration.statesDissimilar.collectAsStateWithLifecycle().value,
            modifier = Modifier.fillMaxWidth(),
        )

        // Show PropertyInfoDialog if applicable
        infoDialogData?.let {
            PropertyInfoDialog(
                data = it,
                onDismissRequest = { infoDialogData = null }
            )
        }
    }
}
