package com.w2sv.wifiwidget.activities.main

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.extensions.disable
import com.w2sv.wifiwidget.extensions.enable
import com.w2sv.wifiwidget.preferences.BooleanPreferences
import com.w2sv.wifiwidget.preferences.WidgetPreferences

@Composable
fun PinAppWidgetButton(
    requestPinWidget: () -> Unit, launchLocationPermissionRequest: () -> Unit
) {
    val triggerOnClickListener = remember {
        mutableStateOf(false)
    }

    ElevatedButton(
        triggerOnClickListener::enable,
        modifier = Modifier.defaultMinSize(140.dp, 60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue_chill_dark)),
        content = {
            Text(
                stringResource(R.string.pin_widget),
                color = Color.White
            )
        }
    )

    if (triggerOnClickListener.value) {
        if (!BooleanPreferences.locationPermissionDialogAnswered)
            LocationPermissionDialog(
                onConfirm = {
                    launchLocationPermissionRequest()
                },
                onDismiss = {
                    WidgetPreferences.showSSID = false
                    requestPinWidget()
                },
                onButtonPress = {
                    BooleanPreferences.locationPermissionDialogAnswered = true
                },
                onClose = triggerOnClickListener::disable
            )
        else {
            requestPinWidget()
            triggerOnClickListener.disable()
        }
    }
}

@Composable
private fun LocationPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onButtonPress: () -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        confirmButton = {
            ElevatedButton({
                onConfirm()
                onButtonPress()
                onClose()
            }) { Text(text = "Go ahead") }
        },
        dismissButton = {
            ElevatedButton({
                onDismiss()
                onButtonPress()
                onClose()
            }) {
                Text(text = "Ignore SSID")
            }
        },
        onDismissRequest = onClose,
        text = { Text(text = "If you want your SSID to be displayed, you'll have to grant location access") }
    )
}