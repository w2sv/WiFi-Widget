package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.wifiproperties

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.w2sv.data.model.WifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.InfoIconButton
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.ParameterCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.ParameterCheckRowData
import com.w2sv.wifiwidget.ui.theme.AppTheme

@Stable
class WifiPropertyCheckRowData(
    type: WifiProperty,
    allowCheckChange: (Boolean) -> Boolean = { true }
) : ParameterCheckRowData<WifiProperty>(type, type.viewData.labelRes, allowCheckChange)

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
                    allowCheckChange = {
                        allowLAPDependentPropertyCheckChange(
                            WifiProperty.SSID,
                            it
                        )
                    }
                ),
                WifiPropertyCheckRowData(
                    type = WifiProperty.BSSID,
                    allowCheckChange = {
                        allowLAPDependentPropertyCheckChange(
                            WifiProperty.BSSID,
                            it
                        )
                    }
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.IP
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.Netmask
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.IPv6Local
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.IPv6Public1
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.IPv6Public2
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.Frequency
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.Channel
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.LinkSpeed
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.Gateway
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.DNS
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.DHCP
                )
            )
        }
            .forEach {
                WifiPropertyCheckRow(
                    data = it,
                    typeCheckedMap = wifiPropertiesMap,
                    onInfoButtonClick = { onInfoButtonClick(it.type) }
                )
            }
    }
}

@Preview
@Composable
private fun WifiPropertyCheckRowPrev() {
    AppTheme(useDynamicTheme = true) {
        WifiPropertyCheckRow(
            data = WifiPropertyCheckRowData(
                WifiProperty.Netmask
            ),
            typeCheckedMap = mutableMapOf(),
            onInfoButtonClick = {}
        )
    }
}

@Composable
private fun WifiPropertyCheckRow(
    data: WifiPropertyCheckRowData,
    typeCheckedMap: MutableMap<WifiProperty, Boolean>,
    onInfoButtonClick: () -> Unit
) {
    val label = stringResource(id = data.labelRes)
    val infoIconCD = stringResource(id = R.string.info_icon_cd).format(label)

    ParameterCheckRow(
        data = data,
        typeToIsChecked = typeCheckedMap,
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