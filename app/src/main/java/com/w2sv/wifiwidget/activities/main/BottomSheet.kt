package com.w2sv.wifiwidget.activities.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.preferences.BooleanPreferences

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetLayout(sheetState: ModalBottomSheetState, content: @Composable () -> Unit) {
    ModalBottomSheetLayout(
        sheetContent = { WifiPropertyConfigurationList() },
        sheetShape = RoundedCornerShape(40.dp, 40.dp),
        sheetState = sheetState,
        sheetBackgroundColor = colorResource(id = R.color.blue_dianne)
    ) {
        content()
    }
}

@Preview
@Composable
fun WifiPropertyConfigurationList() {
    Column(modifier = Modifier.padding(horizontal = 26.dp)) {
        mapOf(
            R.string.ssid to BooleanPreferences::showSSID,
            R.string.ipv4 to BooleanPreferences::showIPv4,
            R.string.frequency to BooleanPreferences::showFrequency,
            R.string.gateway to BooleanPreferences::showGateway,
            R.string.subnet_mask to BooleanPreferences::showSubnetMask,
            R.string.dns to BooleanPreferences::showDNS,
            R.string.dhcp to BooleanPreferences::showDHCP
        )
            .forEach { (stringId, preferenceProperty) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = stringId),
                        color = Color.White,
                        modifier = Modifier.weight(1f, fill = true)
                    )
                    Checkbox(
                        checked = preferenceProperty.get(),
                        onCheckedChange = { preferenceProperty.set(it) }
                    )
                }
                Divider(color = Color.DarkGray)
            }
    }
}