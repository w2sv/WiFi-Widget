package com.w2sv.wifiwidget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.wifiwidget.preferences.BooleanPreferences

@Preview
@Composable
fun BottomSheetPreview() {
    BottomSheetLayout {}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetLayout(content: @Composable () -> Unit) {
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.HalfExpanded,
    )

    val showModalSheet = rememberSaveable {
        mutableStateOf(false)
    }

    ModalBottomSheetLayout(
        sheetContent = { WifiPropertyConfigurationList() },
        sheetShape = RoundedCornerShape(50.dp, 50.dp),
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
                        colors = CheckboxDefaults.colors(checkedColor = colorResource(id = R.color.blue_chill)),
                        onCheckedChange = { preferenceProperty.set(it) }
                    )
                }
                Divider(color = Color.DarkGray)
            }
    }
}