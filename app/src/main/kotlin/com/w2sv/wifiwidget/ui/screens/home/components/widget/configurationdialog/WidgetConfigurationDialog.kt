package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.w2sv.wifiwidget.ui.utils.conditional
import com.w2sv.wifiwidget.ui.utils.isLandscapeModeActivated

@Composable
fun WidgetConfigurationDialog(
    locationAccessState: LocationAccessState,
    widgetConfiguration: UnconfirmedWidgetConfiguration,
    infoDialogData: InfoDialogData?,
    setPropertyInfoDialogData: (InfoDialogData?) -> Unit,
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resetConfigAndCloseDialog: () -> Unit = remember {
        {
            widgetConfiguration.reset()
            closeDialog()
        }
    }

    // Show PropertyInfoDialog if applicable
    infoDialogData?.let {
        PropertyInfoDialog(
            data = it,
            onDismissRequest = { setPropertyInfoDialogData(null) })
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
        modifier = modifier.conditional(
            condition = isLandscapeModeActivated,
            onTrue = { fillMaxHeight() }
        ),
    ) {
        WidgetConfigurationDialogContent(
            widgetConfiguration = widgetConfiguration,
            locationAccessState = locationAccessState,
            showPropertyInfoDialog = setPropertyInfoDialogData,
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
    }
}
