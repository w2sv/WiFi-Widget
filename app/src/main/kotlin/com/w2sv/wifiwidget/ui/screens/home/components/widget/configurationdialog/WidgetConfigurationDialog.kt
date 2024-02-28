package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.wifiwidget.ui.designsystem.ConfigurationDialog
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.ColorPickerDialog
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.CustomColor
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.PropertyInfoDialog
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.InfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.UnconfirmedWidgetConfiguration
import com.w2sv.wifiwidget.ui.utils.isLandscapeModeActivated
import com.w2sv.wifiwidget.ui.utils.thenIf

private const val infoDialogDataRememberKey = "WIDGET_CONFIGURATION_DIALOG_INFO_DIALOG_DATA"
private const val configurationColorRememberKey = "CONFIGURATION_COLOR"

@Composable
fun WidgetConfigurationDialog(
    locationAccessState: LocationAccessState,
    widgetConfiguration: UnconfirmedWidgetConfiguration,
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var infoDialogData by rememberSaveable(
        stateSaver = InfoDialogData.nullableStateSaver,
        key = infoDialogDataRememberKey
    ) {
        mutableStateOf(null)
    }
    var configurationColor by rememberSaveable(
        stateSaver = CustomColor.nullableSaver,
        key = configurationColorRememberKey
    ) {
        mutableStateOf(null)
    }

    ConfigurationDialog(
        onDismissRequest = remember {
            {
                widgetConfiguration.reset()
                closeDialog()
            }
        },
        onApplyButtonPress = remember {
            {
                widgetConfiguration.launchSync()
                closeDialog()
            }
        },
        modifier = modifier.thenIf(
            condition = isLandscapeModeActivated,
            onTrue = { fillMaxHeight() }
        ),
        iconRes = com.w2sv.widget.R.drawable.ic_settings_24,
        title = stringResource(id = com.w2sv.common.R.string.configure_widget),
        applyButtonEnabled = widgetConfiguration.statesDissimilar.collectAsStateWithLifecycle().value,
    ) {
        WidgetConfigurationDialogContent(
            widgetConfiguration = widgetConfiguration,
            locationAccessState = locationAccessState,
            showPropertyInfoDialog = remember { { infoDialogData = it } },
            showCustomColorConfigurationDialog = remember { { configurationColor = it } },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
        )

        // Show PropertyInfoDialog if applicable
        infoDialogData?.let {
            PropertyInfoDialog(
                data = it,
                onDismissRequest = { infoDialogData = null }
            )
        }
        configurationColor?.let {
            ColorPickerDialog(
                label = stringResource(id = it.labelRes),
                appliedColor = it.color,
                applyColor = { },
                onDismissRequest = {
                    configurationColor = null
                },
            )
        }
    }
}
