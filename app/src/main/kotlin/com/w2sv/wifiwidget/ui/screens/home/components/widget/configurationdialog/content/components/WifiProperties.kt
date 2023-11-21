package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.w2sv.data.model.WifiProperty
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.PropertyCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.SubPropertyCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.IPPropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyInfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.WifiPropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.infoDialogData

@Composable
internal fun WifiPropertySelection(
    wifiPropertiesMap: MutableMap<WidgetWifiProperty, Boolean>,
    ipSubPropertiesMap: MutableMap<WidgetWifiProperty.IPProperty.SubProperty, Boolean>,
    allowLAPDependentPropertyCheckChange: (WidgetWifiProperty, Boolean) -> Boolean,
    showInfoDialog: (PropertyInfoDialogData) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        remember {
            listOf(
                WifiPropertyCheckRowData(
                    type = WidgetWifiProperty.SSID,
                    isCheckedMap = wifiPropertiesMap,
                    allowCheckChange = {
                        allowLAPDependentPropertyCheckChange(
                            WidgetWifiProperty.SSID,
                            it,
                        )
                    },
                ),
                WifiPropertyCheckRowData(
                    type = WidgetWifiProperty.BSSID,
                    isCheckedMap = wifiPropertiesMap,
                    allowCheckChange = {
                        allowLAPDependentPropertyCheckChange(
                            WidgetWifiProperty.BSSID,
                            it,
                        )
                    },
                ),
                IPPropertyCheckRowData(
                    WidgetWifiProperty.IPv4,
                    isCheckedMap = wifiPropertiesMap,
                    subPropertyIsCheckedMap = ipSubPropertiesMap,
                ),
                IPPropertyCheckRowData(
                    WidgetWifiProperty.IPv6,
                    isCheckedMap = wifiPropertiesMap,
                    subPropertyIsCheckedMap = ipSubPropertiesMap,
                ),
                WifiPropertyCheckRowData(
                    WidgetWifiProperty.Frequency,
                    isCheckedMap = wifiPropertiesMap,
                ),
                WifiPropertyCheckRowData(
                    WidgetWifiProperty.Channel,
                    isCheckedMap = wifiPropertiesMap,
                ),
                WifiPropertyCheckRowData(
                    WidgetWifiProperty.LinkSpeed,
                    isCheckedMap = wifiPropertiesMap,
                ),
                WifiPropertyCheckRowData(
                    WidgetWifiProperty.Gateway,
                    isCheckedMap = wifiPropertiesMap,
                ),
                WifiPropertyCheckRowData(
                    WidgetWifiProperty.DNS,
                    isCheckedMap = wifiPropertiesMap,
                ),
                WifiPropertyCheckRowData(
                    WidgetWifiProperty.DHCP,
                    isCheckedMap = wifiPropertiesMap,
                ),
            )
        }
            .forEach {
                WifiPropertyCheckRow(
                    data = it,
                    showInfoDialog = showInfoDialog,
                )
            }
    }
}

@Composable
private fun WifiPropertyCheckRow(
    data: WifiPropertyCheckRowData,
    showInfoDialog: (PropertyInfoDialogData) -> Unit,
) {
    Column {
        PropertyCheckRow(
            data = data,
            onInfoButtonClick = { showInfoDialog(data.type.viewData.infoDialogData) },
        )
        if (data is IPPropertyCheckRowData) {
            AnimatedVisibility(visible = data.isChecked()) {
                IPSubPropertyCheckRows(
                    subProperties = (data.type as WidgetWifiProperty.IPProperty).subProperties,
                    subPropertyIsCheckedMap = data.subPropertyIsCheckedMap,
                    showInfoDialog = showInfoDialog,
                )
            }
        }
    }
}

@Composable
private fun IPSubPropertyCheckRows(
    subProperties: List<WidgetWifiProperty.IPProperty.SubProperty>,
    subPropertyIsCheckedMap: MutableMap<WidgetWifiProperty.IPProperty.SubProperty, Boolean>,
    showInfoDialog: (PropertyInfoDialogData) -> Unit,
) {
    Column {
        subProperties.forEach { subProperty ->
            SubPropertyCheckRow(
                data = PropertyCheckRowData(
                    type = subProperty,
                    labelRes = subProperty.viewData.labelRes,
                    isCheckedMap = subPropertyIsCheckedMap,
                    allowCheckChange = { newValue ->
                        mapOf(
                            WidgetWifiProperty.IPv6.includeLocal to WidgetWifiProperty.IPv6.includePublic,
                            WidgetWifiProperty.IPv6.includePublic to WidgetWifiProperty.IPv6.includeLocal,
                        )[subProperty]
                            ?.let { inverseSubProperty ->
                                if (!newValue && !subPropertyIsCheckedMap.getValue(
                                        inverseSubProperty,
                                    )
                                ) {
                                    subPropertyIsCheckedMap[inverseSubProperty] =
                                        true
                                }
                            }

                        true
                    },
                ),
                onInfoButtonClick = { showInfoDialog(subProperty.viewData.infoDialogData) },
            )
        }
    }
}
