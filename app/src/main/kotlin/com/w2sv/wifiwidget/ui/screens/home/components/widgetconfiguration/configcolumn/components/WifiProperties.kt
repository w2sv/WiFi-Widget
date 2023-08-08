package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.w2sv.data.model.WifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.InfoIconButton
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

@Composable
internal fun WifiPropertySelection(
    wifiPropertiesMap: MutableMap<WifiProperty, Boolean>,
    ipSubPropertiesMap: MutableMap<WifiProperty.IPProperty.SubProperty, Boolean>,
    allowLAPDependentPropertyCheckChange: (WifiProperty, Boolean) -> Boolean,
    onInfoButtonClick: (WifiProperty) -> Unit,
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
                    onInfoButtonClick = { onInfoButtonClick(it.type) }
                )
            }
    }
}

@Composable
private fun WifiPropertyCheckRow(
    data: WifiPropertyCheckRowData,
    onInfoButtonClick: () -> Unit
) {
    val label = stringResource(id = data.labelRes)
    val infoIconCD = stringResource(id = R.string.info_icon_cd).format(label)

    Column {
        PropertyCheckRow(
            data = data,
            trailingIconButton = {
                InfoIconButton(
                    onClick = {
                        onInfoButtonClick()
                    },
                    contentDescription = infoIconCD
                )
            }
        )
        if (data is IPPropertyCheckRowData) {
            AnimatedVisibility(visible = data.isChecked()) {
                Column {
                    (data.type as WifiProperty.IPProperty).subProperties.forEach { subProperty ->
                        SubPropertyCheckRow(
                            data = PropertyCheckRowData(
                                type = subProperty,
                                labelRes = subProperty.labelRes,
                                isCheckedMap = data.subPropertyIsCheckedMap,
                                allowCheckChange = { newValue ->
                                    mapOf(
                                        WifiProperty.IPv6.includeLocal to WifiProperty.IPv6.includePublic,
                                        WifiProperty.IPv6.includePublic to WifiProperty.IPv6.includeLocal,
                                    )[subProperty]
                                        ?.let { inverseSubProperty ->
                                            if (!newValue && !data.subPropertyIsCheckedMap.getValue(
                                                    inverseSubProperty
                                                )
                                            ) {
                                                data.subPropertyIsCheckedMap[inverseSubProperty] =
                                                    true
                                            }
                                        }

                                    true
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}