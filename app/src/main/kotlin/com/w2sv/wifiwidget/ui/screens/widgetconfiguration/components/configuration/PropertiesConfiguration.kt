package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.w2sv.domain.model.WifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.DropdownMenuItemProperties
import com.w2sv.wifiwidget.ui.designsystem.IconHeaderProperties
import com.w2sv.wifiwidget.ui.designsystem.InfoIcon
import com.w2sv.wifiwidget.ui.designsystem.MoreIconButtonWithDropdownMenu
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.screens.home.components.EnablePropertyOnReversibleConfiguration
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

fun propertiesConfigurationCard(
    widgetConfiguration: ReversibleWidgetConfiguration,
    locationAccessState: LocationAccessState,
    showInfoDialog: (InfoDialogData) -> Unit
): WidgetConfigurationCard =
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
                                onClick = { widgetConfiguration.restoreDefaultPropertyOrder() },
                                enabled = { !propertiesInDefaultOrder },
                                leadingIconRes = R.drawable.ic_restart_alt_24
                            )
                        )
                    }
                )
            }
        )
    ) {
        PropertyReorderingInformation(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .padding(bottom = 8.dp)
        )
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
    }

@Composable
private fun PropertyReorderingInformation(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WithLocalContentColor(MaterialTheme.colorScheme.onSurfaceVariantLowAlpha) {
            InfoIcon()
            Text(stringResource(R.string.wifi_property_reordering_information), fontSize = 13.sp)
        }
    }
}

@Composable
private fun rememberWidgetWifiPropertyCheckRowData(
    widgetConfiguration: ReversibleWidgetConfiguration,
    locationAccessState: LocationAccessState,
    showInfoDialog: (InfoDialogData) -> Unit
): ImmutableList<ConfigurationColumnElement.CheckRow<WifiProperty>> {
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
): ConfigurationColumnElement.CheckRow<WifiProperty> {
    val shakeController = ShakeController()

    return ConfigurationColumnElement.CheckRow.fromIsCheckedMap(
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
): ImmutableList<ConfigurationColumnElement> =
    buildList {
        if (this@subPropertyElements is WifiProperty.IP.V4AndV6) {
            add(
                ConfigurationColumnElement.Custom {
                    VersionsHeader(modifier = Modifier.padding(top = SubPropertyColumnDefaults.startPadding))
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
                    ConfigurationColumnElement.CheckRow.fromIsCheckedMap(
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
                                onTrue = { padding(start = 16.dp) }
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
): ImmutableList<ConfigurationColumnElement> =
    LocationParameter.entries.map { parameter ->
        val shakeController = ShakeController()
        ConfigurationColumnElement.CheckRow.fromIsCheckedMap(
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
