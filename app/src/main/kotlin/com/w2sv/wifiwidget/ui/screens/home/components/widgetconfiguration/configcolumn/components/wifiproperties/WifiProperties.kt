package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.wifiproperties

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.w2sv.data.model.WifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.InfoIconButton
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.ParameterCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.ParameterCheckRowData

@Stable
class WifiPropertyCheckRowData(
    type: WifiProperty,
    isCheckedMap: MutableMap<WifiProperty, Boolean>,
    allowCheckChange: (Boolean) -> Boolean = { true }
) : ParameterCheckRowData<WifiProperty>(
    type,
    type.viewData.labelRes,
    isCheckedMap,
    allowCheckChange
)

@Composable
internal fun WifiPropertySelection(
    wifiPropertiesMap: MutableMap<WifiProperty, Boolean>,
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
                WifiPropertyCheckRowData(
                    WifiProperty.IP,
                    isCheckedMap = wifiPropertiesMap
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.Netmask,
                    isCheckedMap = wifiPropertiesMap
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.IPv6Local,
                    isCheckedMap = wifiPropertiesMap
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.IPv6Public1,
                    isCheckedMap = wifiPropertiesMap
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.IPv6Public2,
                    isCheckedMap = wifiPropertiesMap
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

    ParameterCheckRow(
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
}