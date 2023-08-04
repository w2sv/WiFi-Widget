package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.wifiproperties

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
    @StringRes labelRes: Int,
    @ArrayRes val arrayRes: Int,
    allowCheckChange: (Boolean) -> Boolean = { true }
) : ParameterCheckRowData<WifiProperty>(type, labelRes, allowCheckChange)

@Composable
internal fun WifiPropertySelection(
    wifiPropertiesMap: MutableMap<WifiProperty, Boolean>,
    allowSSIDCheckChange: (Boolean) -> Boolean,
    onInfoButtonClick: (WifiPropertyCheckRowData) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        remember {
            listOf(
                WifiPropertyCheckRowData(
                    WifiProperty.SSID,
                    com.w2sv.data.R.string.ssid,
                    com.w2sv.data.R.array.ssid,
                    allowSSIDCheckChange
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.BSSID,
                    com.w2sv.data.R.string.bssid,
                    com.w2sv.data.R.array.bssid
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.IP,
                    com.w2sv.data.R.string.ipv4,
                    com.w2sv.data.R.array.ipv4
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.Netmask,
                    com.w2sv.data.R.string.netmask,
                    com.w2sv.data.R.array.netmask
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.IPv6Local,
                    com.w2sv.data.R.string.ipv6_local,
                    com.w2sv.data.R.array.ipv6_local
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.IPv6Public1,
                    com.w2sv.data.R.string.ipv6_public_1,
                    com.w2sv.data.R.array.ipv6_public_1
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.IPv6Public2,
                    com.w2sv.data.R.string.ipv6_public_2,
                    com.w2sv.data.R.array.ipv6_public_2
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.Frequency,
                    com.w2sv.data.R.string.frequency,
                    com.w2sv.data.R.array.frequency
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.Channel,
                    com.w2sv.data.R.string.channel,
                    com.w2sv.data.R.array.channel
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.LinkSpeed,
                    com.w2sv.data.R.string.linkspeed,
                    com.w2sv.data.R.array.linkspeed
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.Gateway,
                    com.w2sv.data.R.string.gateway,
                    com.w2sv.data.R.array.gateway
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.DNS,
                    com.w2sv.data.R.string.dns,
                    com.w2sv.data.R.array.dns
                ),
                WifiPropertyCheckRowData(
                    WifiProperty.DHCP,
                    com.w2sv.data.R.string.dhcp,
                    com.w2sv.data.R.array.dhcp
                )
            )
        }
            .forEach {
                WifiPropertyCheckRow(
                    data = it,
                    typeToIsChecked = wifiPropertiesMap,
                    onInfoButtonClick = onInfoButtonClick
                )
            }
    }
}

@Composable
private fun WifiPropertyCheckRow(
    data: WifiPropertyCheckRowData,
    typeToIsChecked: MutableMap<WifiProperty, Boolean>,
    onInfoButtonClick: (WifiPropertyCheckRowData) -> Unit
) {
    val label = stringResource(id = data.labelRes)
    val infoIconCD = stringResource(id = R.string.info_icon_cd).format(label)

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ParameterCheckRow(data = data, typeToIsChecked = typeToIsChecked)
        InfoIconButton(
            onClick = {
                onInfoButtonClick(data)
            },
            contentDescription = infoIconCD
        )
    }
}