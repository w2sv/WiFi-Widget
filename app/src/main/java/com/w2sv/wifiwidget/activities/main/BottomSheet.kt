package com.w2sv.wifiwidget.activities.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.extensions.toggle
import com.w2sv.wifiwidget.preferences.BooleanPreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Preview(showSystemUi = true)
fun BottomSheetPreview() {
    BottomSheetLayout(
        sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Expanded)
    ) {}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetLayout(sheetState: ModalBottomSheetState, content: @Composable () -> Unit) {
    ModalBottomSheetLayout(
        sheetContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SheetToggleButton(sheetState)
                SheetContent()
            }
        },
        sheetElevation = 0.dp,
        sheetShape = RoundedCornerShape(40.dp, 40.dp),
        sheetState = sheetState,
        sheetBackgroundColor = Color.Transparent
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SheetToggleButton(sheetState: ModalBottomSheetState) {
    val scope = rememberCoroutineScope()

    IconButton(
        onClick = {
            scope.launch {
                if (sheetState.isVisible)
                    sheetState.hide()
                else
                    sheetState.show()
            }
        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Transparent, contentColor = colorResource(
                id = R.color.blue_chill_dark
            )
        )
    ) {
        Icon(
            imageVector = if (sheetState.isVisible)
                Icons.Filled.KeyboardArrowDown
            else
                Icons.Filled.KeyboardArrowUp,
            contentDescription = stringResource(id = R.string.configure_widget)
        )
    }
}

@Preview
@Composable
fun SheetContent() {
    Surface(color = colorResource(id = R.color.mischka_dark)) {
        Text(
            text = "Configure displayed properties",
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontStyle = FontStyle.Italic,
            style = TextStyle(
                color = colorResource(
                    id = R.color.blue_chill_dark
                )
            )
        )
        WifiPropertyConfigurationList()
    }
}

@Preview(showSystemUi = true)
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
                    val checked = remember(preferenceProperty.hashCode()) {
                        mutableStateOf(preferenceProperty.get())
                    }
                    Checkbox(
                        checked = checked.value,
                        onCheckedChange = {
                            checked.toggle()
                            preferenceProperty.set(it)
                        }
                    )
                }
                Divider(color = Color.DarkGray)
            }
    }
}