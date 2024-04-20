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
import com.w2sv.composed.extensions.thenIf
import com.w2sv.composed.isLandscapeModeActive
import com.w2sv.wifiwidget.ui.designsystem.ConfigurationDialog
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.ColorPickerDialog
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.ColorPickerProperties
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.PropertyInfoDialog
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.InfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.ReversibleWidgetConfiguration
import kotlinx.coroutines.flow.update

@Composable
fun WidgetConfigurationDialog(
    locationAccessState: LocationAccessState,
    widgetConfiguration: ReversibleWidgetConfiguration,
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var infoDialogData by rememberSaveable(
        stateSaver = InfoDialogData.nullableStateSaver,
    ) {
        mutableStateOf(null)
    }
    var colorPickerProperties by rememberSaveable(
        stateSaver = ColorPickerProperties.nullableStateSaver,
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
            condition = isLandscapeModeActive,
            onTrue = { fillMaxHeight() }
        ),
        iconRes = com.w2sv.core.common.R.drawable.ic_settings_24,
        title = stringResource(id = com.w2sv.core.common.R.string.configure_widget),
        applyButtonEnabled = widgetConfiguration.statesDissimilar.collectAsStateWithLifecycle().value,
    ) {
        WidgetConfigurationDialogContent(
            widgetConfiguration = widgetConfiguration,
            locationAccessState = locationAccessState,
            showPropertyInfoDialog = remember { { infoDialogData = it } },
            showCustomColorConfigurationDialog = remember { { colorPickerProperties = it } },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
        )

        // Show PropertyInfoDialog if applicable
        infoDialogData?.let {
            PropertyInfoDialog(
                data = it,
                onDismissRequest = remember {
                    { infoDialogData = null }
                }
            )
        }
        // Show ColorPickerDialog if applicable
        colorPickerProperties?.let { properties ->
            ColorPickerDialog(
                properties = properties,
                applyColor = remember {
                    {
                        widgetConfiguration.coloringConfig.update {
                            it.copy(
                                custom = properties.createCustomColoringData(
                                    it.custom
                                )
                            )
                        }

                    }
                },
                onDismissRequest = remember {
                    {
                        colorPickerProperties = null
                    }
                },
            )
        }
    }
}
