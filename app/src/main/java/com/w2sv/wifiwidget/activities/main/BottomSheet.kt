package com.w2sv.wifiwidget.activities.main

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetState
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.preferences.WidgetPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheet(sheetState: BottomSheetState) {
    val coroutineScope = rememberCoroutineScope()

    BackHandler(sheetState.isExpanded) {
        coroutineScope.launch { sheetState.collapse() }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ToggleButton(sheetState, coroutineScope)
        SheetContent()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ToggleButton(sheetState: BottomSheetState, coroutineScope: CoroutineScope) {
    IconButton(
        onClick = {
            coroutineScope.launch {
                if (sheetState.isExpanded)
                    sheetState.collapse()
                else
                    sheetState.expand()
            }
        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Transparent, contentColor = colorResource(
                id = R.color.blue_chill_dark
            )
        )
    ) {
        Icon(
            imageVector = if (sheetState.isExpanded)
                Icons.Filled.KeyboardArrowDown
            else
                Icons.Filled.KeyboardArrowUp,
            contentDescription = stringResource(id = R.string.configure_widget)
        )
    }
}

@Preview
@Composable
private fun SheetContent() {
    Surface(
        modifier = Modifier.padding(horizontal = 30.dp),
        color = colorResource(id = R.color.mischka_dark),
        shape = RoundedCornerShape(40.dp, 40.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                text = "Displayed Properties",
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
}

@Composable
private fun WifiPropertyConfigurationList() {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(horizontal = 26.dp)) {
        mapOf(
            R.string.ssid to "showSSID",
            R.string.ipv4 to "showIPv4",
            R.string.frequency to "showFrequency",
            R.string.gateway to "showGateway",
            R.string.subnet_mask to "showSubnetMask",
            R.string.dns to "showDNS",
            R.string.dhcp to "showDHCP"
        )
            .forEach { (stringId, preferenceKey) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = stringId),
                        color = Color.White,
                        modifier = Modifier.weight(1f, fill = true)
                    )
                    val checked = remember(key1 = preferenceKey.hashCode()) {
                        mutableStateOf(WidgetPreferences.getValue(preferenceKey))
                    }
                    Checkbox(
                        checked = checked.value,
                        onCheckedChange = {
                            if (!it && WidgetPreferences.all { (k, v) -> k == preferenceKey || !v })
                                Toast
                                    .makeText(
                                        context,
                                        "You need to leave at least one property checked!",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            else {
                                checked.value = it
                                WidgetPreferences[preferenceKey] = it
                            }
                        }
                    )
                }
                Divider(color = Color.DarkGray)
            }
    }
}