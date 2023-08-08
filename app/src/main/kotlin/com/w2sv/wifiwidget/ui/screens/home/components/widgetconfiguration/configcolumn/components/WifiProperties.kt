package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.w2sv.data.model.WifiProperty
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.InfoDialogButtonData
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.InfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.PropertyCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.PropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.SubPropertyCheckRow

@Stable
open class WifiPropertyCheckRowData(
    type: WifiProperty,
    isCheckedMap: MutableMap<WifiProperty, Boolean>,
    allowCheckChange: (Boolean) -> Boolean = { true },
) : PropertyCheckRowData<WifiProperty>(
    type,
    type.viewData.labelRes,
    isCheckedMap,
    allowCheckChange
)

@Stable
class IPPropertyCheckRowData(
    type: WifiProperty.IPProperty,
    isCheckedMap: MutableMap<WifiProperty, Boolean>,
    val subPropertyIsCheckedMap: MutableMap<WifiProperty.IPProperty.SubProperty, Boolean>,
    allowCheckChange: (Boolean) -> Boolean = { true }
) : WifiPropertyCheckRowData(type, isCheckedMap, allowCheckChange)

private val WifiProperty.ViewData.infoDialogData: InfoDialogData
    get() = InfoDialogData(
        labelRes,
        descriptionRes,
        learnMoreUrl
    )

@Composable
internal fun WifiPropertySelection(
    wifiPropertiesMap: MutableMap<WifiProperty, Boolean>,
    ipSubPropertiesMap: MutableMap<WifiProperty.IPProperty.SubProperty, Boolean>,
    allowLAPDependentPropertyCheckChange: (WifiProperty, Boolean) -> Boolean,
    showInfoDialog: (InfoDialogData) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        remember {
            listOf(
                WifiPropertyCheckRowData(
                    type = WifiProperty.SSID,
                    isCheckedMap = wifiPropertiesMap,
                    allowCheckChange = {
                        allowLAPDependentPropertyCheckChange(
                            WifiProperty.SSID,
                            it
                        )
                    }
                ),
                WifiPropertyCheckRowData(
                    type = WifiProperty.BSSID,
                    isCheckedMap = wifiPropertiesMap,
                    allowCheckChange = {
                        allowLAPDependentPropertyCheckChange(
                            WifiProperty.BSSID,
                            it
                        )
                    }
                ),
                IPPropertyCheckRowData(
                    WifiProperty.IPv4,
                    isCheckedMap = wifiPropertiesMap,
                    subPropertyIsCheckedMap = ipSubPropertiesMap
                ),
                IPPropertyCheckRowData(
                    WifiProperty.IPv6,
                    isCheckedMap = wifiPropertiesMap,
                    subPropertyIsCheckedMap = ipSubPropertiesMap
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.Frequency,
                    isCheckedMap = wifiPropertiesMap
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.Channel,
                    isCheckedMap = wifiPropertiesMap
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.LinkSpeed,
                    isCheckedMap = wifiPropertiesMap
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.Gateway,
                    isCheckedMap = wifiPropertiesMap
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.DNS,
                    isCheckedMap = wifiPropertiesMap
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.DHCP,
                    isCheckedMap = wifiPropertiesMap
                )
            )
        }
            .forEach {
                WifiPropertyCheckRow(
                    data = it,
                    showInfoDialog = showInfoDialog
                )
            }
    }
}

@Composable
private fun WifiPropertyCheckRow(
    data: WifiPropertyCheckRowData,
    showInfoDialog: (InfoDialogData) -> Unit
) {
    Column {
        PropertyCheckRow(
            data = data,
            infoDialogButtonData = InfoDialogButtonData { showInfoDialog(data.type.viewData.infoDialogData) }
        )
        if (data is IPPropertyCheckRowData) {
            AnimatedVisibility(visible = data.isChecked()) {
                IPSubPropertyCheckRows(
                    subProperties = (data.type as WifiProperty.IPProperty).subProperties,
                    subPropertyIsCheckedMap = data.subPropertyIsCheckedMap,
                    showInfoDialog = showInfoDialog
                )
            }
        }
    }
}

@Composable
private fun IPSubPropertyCheckRows(
    subProperties: List<WifiProperty.IPProperty.SubProperty>,
    subPropertyIsCheckedMap: MutableMap<WifiProperty.IPProperty.SubProperty, Boolean>,
    showInfoDialog: (InfoDialogData) -> Unit
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
                            WifiProperty.IPv6.includeLocal to WifiProperty.IPv6.includePublic,
                            WifiProperty.IPv6.includePublic to WifiProperty.IPv6.includeLocal,
                        )[subProperty]
                            ?.let { inverseSubProperty ->
                                if (!newValue && !subPropertyIsCheckedMap.getValue(
                                        inverseSubProperty
                                    )
                                ) {
                                    subPropertyIsCheckedMap[inverseSubProperty] =
                                        true
                                }
                            }

                        true
                    }
                ),
                infoDialogButtonData = InfoDialogButtonData { showInfoDialog(subProperty.viewData.infoDialogData) }
            )
        }
    }
}