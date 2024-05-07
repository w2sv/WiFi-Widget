package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration_column

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.domain.model.WidgetBottomRowElement
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.IconHeaderProperties
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.designsystem.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.screens.home.components.LocationAccessPermissionRequestTrigger
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.ColorPickerDialogData
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.InfoDialogData
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.toInfoDialogData
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.ReversibleWidgetConfiguration
import com.w2sv.wifiwidget.ui.states.LocationAccessState
import com.w2sv.wifiwidget.ui.utils.ShakeConfig
import com.w2sv.wifiwidget.ui.utils.ShakeController
import com.w2sv.wifiwidget.ui.utils.shake
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val subPropertyCheckRowColumnModifier: Modifier = Modifier.padding(horizontal = 16.dp)
private val checkRowColumnBottomPadding = 8.dp

@Immutable
data class SectionCardProperties(
    val iconHeaderProperties: IconHeaderProperties,
    val content: @Composable () -> Unit
)

@Composable
fun rememberSectionCardProperties(
    widgetConfiguration: ReversibleWidgetConfiguration,
    locationAccessState: LocationAccessState,
    showPropertyInfoDialog: (InfoDialogData) -> Unit,
    showCustomColorConfigurationDialog: (ColorPickerDialogData) -> Unit,
    showRefreshIntervalConfigurationDialog: () -> Unit
): ImmutableList<SectionCardProperties> {
    val context: Context = LocalContext.current

    return remember {
        persistentListOf(
            SectionCardProperties(
                iconHeaderProperties = IconHeaderProperties(
                    iconRes = R.drawable.ic_palette_24,
                    stringRes = R.string.appearance
                ),
            ) {
                AppearanceConfiguration(
                    coloringConfig = widgetConfiguration.coloringConfig.collectAsStateWithLifecycle().value,
                    setColoringConfig = remember {
                        {
                            widgetConfiguration.coloringConfig.value = it
                        }
                    },
                    opacity = widgetConfiguration.opacity.collectAsStateWithLifecycle().value,
                    setOpacity = remember {
                        {
                            widgetConfiguration.opacity.value = it
                        }
                    },
                    fontSize = widgetConfiguration.fontSize.collectAsStateWithLifecycle().value,
                    setFontSize = remember {
                        { widgetConfiguration.fontSize.value = it }
                    },
                    showCustomColorConfigurationDialog = showCustomColorConfigurationDialog,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )
            },
            SectionCardProperties(
                IconHeaderProperties(
                    iconRes = R.drawable.ic_checklist_24,
                    stringRes = R.string.properties
                )
            ) {
                PropertyCheckRowColumn(
                    dataList = rememberWidgetWifiPropertyCheckRowData(
                        widgetConfiguration = widgetConfiguration,
                        locationAccessState = locationAccessState
                    ),
                    showInfoDialog = showPropertyInfoDialog,
                )
            },
            SectionCardProperties(
                iconHeaderProperties = IconHeaderProperties(
                    iconRes = R.drawable.ic_bottom_row_24,
                    stringRes = R.string.bottom_bar,
                )
            ) {
                PropertyCheckRowColumn(
                    dataList = remember {
                        WidgetBottomRowElement.entries.map {
                            PropertyConfigurationView.CheckRow.fromIsCheckedMap(
                                property = it,
                                isCheckedMap = widgetConfiguration.bottomRowMap
                            )
                        }
                            .toPersistentList()
                    },
                    modifier = Modifier.padding(bottom = checkRowColumnBottomPadding)
                )
            },
            SectionCardProperties(
                iconHeaderProperties = IconHeaderProperties(
                    iconRes = com.w2sv.core.common.R.drawable.ic_refresh_24,
                    stringRes = R.string.refreshing,
                )
            ) {
                PropertyCheckRowColumn(
                    dataList = remember {
                        persistentListOf(
                            PropertyConfigurationView.CheckRow.fromIsCheckedMap(
                                property = WidgetRefreshingParameter.RefreshPeriodically,
                                isCheckedMap = widgetConfiguration.refreshingParametersMap,
                                infoDialogData = InfoDialogData(
                                    title = context.getString(WidgetRefreshingParameter.RefreshPeriodically.labelRes),
                                    description = context.getString(R.string.refresh_periodically_info)
                                ),
                                subPropertyCheckRowDataList = persistentListOf(
                                    PropertyConfigurationView.Custom {
                                        RefreshIntervalConfigurationRow(
                                            interval = widgetConfiguration.refreshInterval.collectAsStateWithLifecycle().value,
                                            showConfigurationDialog = showRefreshIntervalConfigurationDialog,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        )
                                    },
                                    PropertyConfigurationView.CheckRow.fromIsCheckedMap(
                                        property = WidgetRefreshingParameter.RefreshOnLowBattery,
                                        isCheckedMap = widgetConfiguration.refreshingParametersMap
                                    )
                                ),
                                subPropertyCheckRowColumnModifier = subPropertyCheckRowColumnModifier
                            )
                        )
                    },
                    showInfoDialog = showPropertyInfoDialog,
                    modifier = Modifier.padding(bottom = checkRowColumnBottomPadding)
                )
            }
        )
    }
}

@Composable
private fun rememberWidgetWifiPropertyCheckRowData(
    widgetConfiguration: ReversibleWidgetConfiguration,
    locationAccessState: LocationAccessState,
): ImmutableList<PropertyConfigurationView.CheckRow<WidgetWifiProperty>> {
    val context = LocalContext.current
    val snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current
    val scope: CoroutineScope = rememberCoroutineScope()

    val showLeaveAtLeastOnePropertyEnabledSnackbar: () -> Unit = remember {
        {
            scope.launch {
                snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                    AppSnackbarVisuals(
                        msg = context.getString(R.string.leave_at_least_one_property_enabled),
                        kind = SnackbarKind.Error
                    )
                )
            }
        }
    }

    val showLeaveAtLeastOneAddressVersionEnabledSnackbar: () -> Unit = remember {
        {
            scope.launch {
                snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                    AppSnackbarVisuals(
                        msg = context.getString(R.string.leave_at_least_one_address_version_enabled),
                        kind = SnackbarKind.Error
                    )
                )
            }
        }
    }

    return remember {
        WidgetWifiProperty.entries
            .map { property ->
                property.checkRow(
                    widgetConfiguration = widgetConfiguration,
                    locationAccessState = locationAccessState,
                    showLeaveAtLeastOnePropertyEnabledSnackbar = showLeaveAtLeastOnePropertyEnabledSnackbar,
                    showLeaveAtLeastOneAddressVersionEnabledSnackbar = showLeaveAtLeastOneAddressVersionEnabledSnackbar,
                    context = context
                )
            }
            .toPersistentList()
    }
}

private fun WidgetWifiProperty.checkRow(
    widgetConfiguration: ReversibleWidgetConfiguration,
    locationAccessState: LocationAccessState,
    showLeaveAtLeastOnePropertyEnabledSnackbar: () -> Unit,
    showLeaveAtLeastOneAddressVersionEnabledSnackbar: () -> Unit,
    context: Context
): PropertyConfigurationView.CheckRow<WidgetWifiProperty> {
    val shakeController = ShakeController(shakeConfig)

    return PropertyConfigurationView.CheckRow.fromIsCheckedMap(
        property = this,
        isCheckedMap = widgetConfiguration.wifiProperties,
        allowCheckChange = { isCheckedNew ->
            if (this is WidgetWifiProperty.NonIP.LocationAccessRequiring && isCheckedNew) {
                return@fromIsCheckedMap locationAccessState.isGranted.also {
                    if (!it) {
                        locationAccessState.launchRequest(
                            LocationAccessPermissionRequestTrigger.PropertyCheckChange(
                                this,
                            )
                        )
                    }
                }
            }
            (isCheckedNew || widgetConfiguration.moreThanOnePropertyChecked()).also {
                if (!it) {
                    showLeaveAtLeastOnePropertyEnabledSnackbar()
                }
            }
        },
        onCheckedChangedDisallowed = { shakeController.shake() },
        subPropertyCheckRowDataList = when (this) {
            is WidgetWifiProperty.IP -> {
                subProperties
                    .map { subProperty ->
                        val subPropertyShakeController =
                            if (subProperty.isAddressTypeEnablementProperty)
                                ShakeController(shakeConfig)
                            else
                                null

                        PropertyConfigurationView.CheckRow.fromIsCheckedMap(
                            property = subProperty,
                            isCheckedMap = widgetConfiguration.ipSubProperties,
                            allowCheckChange = { newValue ->
                                subProperty.allowCheckedChange(
                                    newValue,
                                    widgetConfiguration.ipSubProperties
                                )
                            },
                            onCheckedChangedDisallowed = {
                                subPropertyShakeController?.shake()
                                showLeaveAtLeastOneAddressVersionEnabledSnackbar()
                            },
                            modifier = subPropertyShakeController?.let {
                                Modifier.shake(
                                    it
                                )
                            }
                                ?: Modifier
                        )
                    }
                    .toPersistentList()
            }
            else -> persistentListOf()
        } ,
        subPropertyCheckRowColumnModifier = subPropertyCheckRowColumnModifier,
        infoDialogData = toInfoDialogData(context),
        modifier = Modifier.shake(shakeController)
    )
}

private fun ReversibleWidgetConfiguration.moreThanOnePropertyChecked(): Boolean =
    wifiProperties.values.count { it } > 1

private val shakeConfig = ShakeConfig(
    iterations = 2,
    translateX = 12.5f,
    stiffness = 20_000f
)

private fun WidgetWifiProperty.IP.SubProperty.allowCheckedChange(
    newValue: Boolean,
    subPropertyEnablementMap: Map<WidgetWifiProperty.IP.SubProperty, Boolean>
): Boolean =
    when (val capturedKind = kind) {
        is WidgetWifiProperty.IP.V4AndV6.AddressTypeEnablement -> {
            newValue || subPropertyEnablementMap.getValue(
                WidgetWifiProperty.IP.SubProperty(
                    property = property,
                    kind = capturedKind.opposingAddressTypeEnablement
                )
            )
        }

        else -> true
    }