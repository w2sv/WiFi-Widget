package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.composed.extensions.thenIf
import com.w2sv.domain.model.LocationParameter
import com.w2sv.domain.model.WidgetBottomBarElement
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WifiProperty
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.DropdownMenuItemProperties
import com.w2sv.wifiwidget.ui.designsystem.IconHeaderProperties
import com.w2sv.wifiwidget.ui.designsystem.InfoIcon
import com.w2sv.wifiwidget.ui.designsystem.MoreIconButtonWithDropdownMenu
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.screens.home.components.EnablePropertyOnReversibleConfiguration
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.ColorPickerDialogData
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.InfoDialogData
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.infoDialogData
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.ReversibleWidgetConfiguration
import com.w2sv.wifiwidget.ui.states.LocationAccessState
import com.w2sv.wifiwidget.ui.theme.onSurfaceVariantLowAlpha
import com.w2sv.wifiwidget.ui.utils.ShakeController
import com.w2sv.wifiwidget.ui.utils.WithLocalContentColor
import com.w2sv.wifiwidget.ui.utils.rememberSnackbarEmitter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val checkRowColumnBottomPadding = 8.dp

@Immutable
data class WidgetConfigurationCard(val iconHeaderProperties: IconHeaderProperties, val content: @Composable () -> Unit)

@Composable
fun rememberWidgetConfigurationCardProperties(
    widgetConfiguration: ReversibleWidgetConfiguration,
    locationAccessState: LocationAccessState,
    showInfoDialog: (InfoDialogData) -> Unit,
    showCustomColorConfigurationDialog: (ColorPickerDialogData) -> Unit,
    showRefreshIntervalConfigurationDialog: () -> Unit
): ImmutableList<WidgetConfigurationCard> =
    remember {
        persistentListOf(
            WidgetConfigurationCard(
                iconHeaderProperties = IconHeaderProperties(
                    iconRes = R.drawable.ic_palette_24,
                    stringRes = R.string.appearance
                )
            ) {
                val coloringConfig by widgetConfiguration.coloringConfig.collectAsStateWithLifecycle()
                val opacity by widgetConfiguration.opacity.collectAsStateWithLifecycle()
                val fontSize by widgetConfiguration.fontSize.collectAsStateWithLifecycle()
                val propertyValueAlignment by widgetConfiguration.propertyValueAlignment.collectAsStateWithLifecycle()

                AppearanceConfiguration(
                    coloringConfig = coloringConfig,
                    setColoringConfig = { widgetConfiguration.coloringConfig.value = it },
                    opacity = opacity,
                    setOpacity = { widgetConfiguration.opacity.value = it },
                    fontSize = fontSize,
                    setFontSize = { widgetConfiguration.fontSize.value = it },
                    showCustomColorConfigurationDialog = showCustomColorConfigurationDialog,
                    propertyValueAlignment = propertyValueAlignment,
                    setPropertyValueAlignment = { widgetConfiguration.propertyValueAlignment.value = it },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            },
            WidgetConfigurationCard(
                IconHeaderProperties(
                    iconRes = R.drawable.ic_checklist_24,
                    stringRes = R.string.properties,
                    trailingIcon = {
                        val propertiesInDefaultOrder by widgetConfiguration.propertiesInDefaultOrder.collectAsStateWithLifecycle()
                        MoreIconButtonWithDropdownMenu(
                            menuItems = remember {
                                persistentListOf(
                                    DropdownMenuItemProperties(
                                        R.string.restore_default_order,
                                        onClick = {
                                            widgetConfiguration.restoreDefaultPropertyOrder()
                                        },
                                        enabled = { !propertiesInDefaultOrder },
                                        leadingIconRes = R.drawable.ic_restart_alt_24
                                    )
                                )
                            }
                        )
                    }
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WithLocalContentColor(MaterialTheme.colorScheme.onSurfaceVariantLowAlpha) {
                        InfoIcon()
                        Text(stringResource(R.string.wifi_property_reordering_information), fontSize = 13.sp)
                    }
                }
                DragAndDroppableCheckRowColumn(
                    elements = rememberWidgetWifiPropertyCheckRowData(
                        widgetConfiguration = widgetConfiguration,
                        locationAccessState = locationAccessState,
                        showInfoDialog = showInfoDialog
                    ),
                    onDrop = { fromIndex: Int, toIndex: Int ->
                        widgetConfiguration
                            .wifiPropertyOrder
                            .update {
                                it
                                    .toMutableList()
                                    .apply { add(toIndex, removeAt(fromIndex)) }
                            }
                    }
                )
            },
            WidgetConfigurationCard(
                iconHeaderProperties = IconHeaderProperties(
                    iconRes = R.drawable.ic_bottom_row_24,
                    stringRes = R.string.bottom_bar
                )
            ) {
                CheckRowColumn(
                    elements = remember {
                        WidgetBottomBarElement.entries.map {
                            CheckRowColumnElement.CheckRow.fromIsCheckedMap(
                                property = it,
                                explanation = it.explanation,
                                isCheckedMap = widgetConfiguration.bottomRowMap
                            )
                        }
                            .toPersistentList()
                    },
                    modifier = Modifier.padding(bottom = checkRowColumnBottomPadding)
                )
            },
            WidgetConfigurationCard(
                iconHeaderProperties = IconHeaderProperties(
                    iconRes = com.w2sv.core.common.R.drawable.ic_refresh_24,
                    stringRes = R.string.refreshing
                )
            ) {
                CheckRowColumn(
                    elements = remember {
                        persistentListOf(
                            CheckRowColumnElement.CheckRow.fromIsCheckedMap(
                                property = WidgetRefreshingParameter.RefreshPeriodically,
                                isCheckedMap = widgetConfiguration.refreshingParametersMap,
                                showInfoDialog = {
                                    showInfoDialog(
                                        InfoDialogData(
                                            titleRes = WidgetRefreshingParameter.RefreshPeriodically.labelRes,
                                            descriptionRes = R.string.refresh_periodically_info
                                        )
                                    )
                                },
                                subPropertyColumnElements = persistentListOf(
                                    CheckRowColumnElement.Custom {
                                        val refreshInterval by widgetConfiguration.refreshInterval.collectAsStateWithLifecycle()
                                        RefreshIntervalConfigurationRow(
                                            interval = refreshInterval,
                                            showConfigurationDialog = showRefreshIntervalConfigurationDialog,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                        )
                                    },
                                    CheckRowColumnElement.CheckRow.fromIsCheckedMap(
                                        property = WidgetRefreshingParameter.RefreshOnLowBattery,
                                        isCheckedMap = widgetConfiguration.refreshingParametersMap
                                    )
                                )
                            )
                        )
                    },
                    modifier = Modifier.padding(bottom = checkRowColumnBottomPadding)
                )
            }
        )
    }

@Composable
private fun rememberWidgetWifiPropertyCheckRowData(
    widgetConfiguration: ReversibleWidgetConfiguration,
    locationAccessState: LocationAccessState,
    showInfoDialog: (InfoDialogData) -> Unit
): ImmutableList<CheckRowColumnElement.CheckRow<WifiProperty>> {
    val scope: CoroutineScope = rememberCoroutineScope()
    val snackbarEmitter = rememberSnackbarEmitter()

    val orderedWifiProperties by widgetConfiguration.wifiPropertyOrder.collectAsStateWithLifecycle()
    return remember(orderedWifiProperties) {
        orderedWifiProperties
            .map { property ->
                property.checkRow(
                    widgetConfiguration = widgetConfiguration,
                    locationAccessState = locationAccessState,
                    showInfoDialog = showInfoDialog,
                    showLeaveAtLeastOnePropertyEnabledSnackbar = {
                        snackbarEmitter.dismissCurrentAndShow(scope) {
                            AppSnackbarVisuals(
                                msg = getString(R.string.leave_at_least_one_property_enabled),
                                kind = SnackbarKind.Warning
                            )
                        }
                    },
                    showLeaveAtLeastOneAddressVersionEnabledSnackbar = {
                        snackbarEmitter.dismissCurrentAndShow(scope) {
                            AppSnackbarVisuals(
                                msg = getString(R.string.leave_at_least_one_address_version_enabled),
                                kind = SnackbarKind.Warning
                            )
                        }
                    },
                    shakeScope = scope
                )
            }
            .toPersistentList()
    }
}

private fun WifiProperty.checkRow(
    widgetConfiguration: ReversibleWidgetConfiguration,
    locationAccessState: LocationAccessState,
    showInfoDialog: (InfoDialogData) -> Unit,
    showLeaveAtLeastOnePropertyEnabledSnackbar: () -> Unit,
    showLeaveAtLeastOneAddressVersionEnabledSnackbar: () -> Unit,
    shakeScope: CoroutineScope
): CheckRowColumnElement.CheckRow<WifiProperty> {
    val shakeController = ShakeController()

    return CheckRowColumnElement.CheckRow.fromIsCheckedMap(
        property = this,
        isCheckedMap = widgetConfiguration.wifiProperties,
        allowCheckChange = { isCheckedNew ->
            if (this is WifiProperty.NonIP.LocationAccessRequiring && isCheckedNew) {
                return@fromIsCheckedMap locationAccessState.allPermissionsGranted.also {
                    if (!it) {
                        locationAccessState.launchMultiplePermissionRequest(EnablePropertyOnReversibleConfiguration(this))
                    }
                }
            }
            (isCheckedNew || widgetConfiguration.wifiProperties.moreThanOnePropertyEnabled()).also {
                if (!it) {
                    showLeaveAtLeastOnePropertyEnabledSnackbar()
                }
            }
        },
        onCheckedChangedDisallowed = { shakeScope.launch { shakeController.shake() } },
        shakeController = shakeController,
        subPropertyColumnElements = when (this) {
            is WifiProperty.IP ->
                subPropertyElements(
                    ipSubPropertyEnablementMap = widgetConfiguration.ipSubProperties,
                    showLeaveAtLeastOneAddressVersionEnabledSnackbar = showLeaveAtLeastOneAddressVersionEnabledSnackbar,
                    shakeScope = shakeScope
                )

            is WifiProperty.NonIP.Other.Location -> subPropertyElements(
                locationParameters = widgetConfiguration.locationParameters,
                showLeaveAtLeastOnePropertyEnabledSnackbar = showLeaveAtLeastOnePropertyEnabledSnackbar,
                scope = shakeScope
            )

            else -> null
        },
        showInfoDialog = { showInfoDialog(infoDialogData()) }
    )
}

private fun Map<*, Boolean>.moreThanOnePropertyEnabled(): Boolean =
    values.count { it } > 1

private fun WifiProperty.IP.subPropertyElements(
    ipSubPropertyEnablementMap: MutableMap<WifiProperty.IP.SubProperty, Boolean>,
    showLeaveAtLeastOneAddressVersionEnabledSnackbar: () -> Unit,
    shakeScope: CoroutineScope
): ImmutableList<CheckRowColumnElement> =
    buildList {
        if (this@subPropertyElements is WifiProperty.IP.V4AndV6) {
            add(
                CheckRowColumnElement.Custom {
                    VersionsHeader(modifier = Modifier.padding(top = SubPropertyColumnDefaults.verticalPadding))
                }
            )
        }
        subProperties
            .forEach { subProperty ->
                val shakeController =
                    if (subProperty.isAddressTypeEnablementProperty) {
                        ShakeController()
                    } else {
                        null
                    }

                add(
                    CheckRowColumnElement.CheckRow.fromIsCheckedMap(
                        property = subProperty,
                        isCheckedMap = ipSubPropertyEnablementMap,
                        allowCheckChange = { newValue ->
                            subProperty.allowCheckedChange(
                                newValue,
                                ipSubPropertyEnablementMap
                            )
                        },
                        onCheckedChangedDisallowed = {
                            shakeScope.launch { shakeController?.shake() }
                            showLeaveAtLeastOneAddressVersionEnabledSnackbar()
                        },
                        show = {
                            if (subProperty.kind == WifiProperty.IP.SubProperty.Kind.ShowSubnetMask) {
                                ipSubPropertyEnablementMap.getValue(
                                    subProperty.copy(kind = WifiProperty.IP.V4AndV6.AddressTypeEnablement.V4Enabled)
                                )
                            } else {
                                true
                            }
                        },
                        shakeController = shakeController,
                        modifier = Modifier
                            .thenIf(
                                condition = subProperty.isAddressTypeEnablementProperty,
                                onTrue = { padding(start = 8.dp) }
                            )
                    )
                )
            }
    }
        .toPersistentList()

private fun subPropertyElements(
    locationParameters: MutableMap<LocationParameter, Boolean>,
    showLeaveAtLeastOnePropertyEnabledSnackbar: () -> Unit,
    scope: CoroutineScope
): ImmutableList<CheckRowColumnElement> =
    LocationParameter.entries.map { parameter ->
        val shakeController = ShakeController()
        CheckRowColumnElement.CheckRow.fromIsCheckedMap(
            property = parameter,
            isCheckedMap = locationParameters,
            allowCheckChange = { newValue ->
                (newValue || locationParameters.moreThanOnePropertyEnabled()).also {
                    if (!it) {
                        scope.launch { shakeController.shake() }
                        showLeaveAtLeastOnePropertyEnabledSnackbar()
                    }
                }
            },
            shakeController = shakeController
        )
    }
        .toPersistentList()

private fun WifiProperty.IP.SubProperty.allowCheckedChange(
    newValue: Boolean,
    subPropertyEnablementMap: Map<WifiProperty.IP.SubProperty, Boolean>
): Boolean =
    when (val capturedKind = kind) {
        is WifiProperty.IP.V4AndV6.AddressTypeEnablement -> {
            newValue ||
                subPropertyEnablementMap.getValue(
                    WifiProperty.IP.SubProperty(
                        property = property,
                        kind = capturedKind.opposingAddressTypeEnablement
                    )
                )
        }

        else -> true
    }
