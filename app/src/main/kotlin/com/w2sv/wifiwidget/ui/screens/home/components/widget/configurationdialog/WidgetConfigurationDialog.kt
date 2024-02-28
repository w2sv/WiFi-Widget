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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.wifiwidget.ui.designsystem.ConfigurationDialog
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.ColorPickerDialog
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.PropertyInfoDialog
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.WidgetColorType
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.InfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.UnconfirmedWidgetConfiguration
import com.w2sv.wifiwidget.ui.utils.colorSaver
import com.w2sv.wifiwidget.ui.utils.isLandscapeModeActivated
import com.w2sv.wifiwidget.ui.utils.nullableColorSaver
import com.w2sv.wifiwidget.ui.utils.thenIf

private const val infoDialogDataRememberKey = "WIDGET_CONFIGURATION_DIALOG_INFO_DIALOG_DATA"
private const val configurationColorRememberKey = "CONFIGURATION_WIDGET_COLOR_TYPE"
private const val colorRememberKey = "COLOR_PICKER_DIALOG_COLOR"

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
    var configurationWidgetColorType by rememberSaveable(
        key = configurationColorRememberKey
    ) {
        mutableStateOf<WidgetColorType?>(null)
    }
    var color by rememberSaveable(
        configurationWidgetColorType,
        key = colorRememberKey,
        stateSaver = nullableColorSaver
    ) {
        mutableStateOf(
            configurationWidgetColorType?.getColor(widgetConfiguration.customColoringData.value)
        )
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
            showCustomColorConfigurationDialog = remember { { configurationWidgetColorType = it } },
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
        if (configurationWidgetColorType != null && color != null) {
            ColorPickerDialog(
                label = stringResource(id = configurationWidgetColorType!!.labelRes),
                color = color!!,
                setColor = { color = it },
                appliedColor = remember(
                    configurationWidgetColorType,
                    widgetConfiguration.customColoringData.value
                ) {
                    configurationWidgetColorType!!.getColor(widgetConfiguration.customColoringData.value)
                },
                applyColor = remember {
                    {
                        widgetConfiguration.customColoringData.value =
                            configurationWidgetColorType!!.setColor(
                                data = widgetConfiguration.customColoringData.value,
                                color = it
                            )
                    }
                },
                onDismissRequest = {
                    configurationWidgetColorType = null
                },
            )
        }
    }
}
