package com.w2sv.wifiwidget.screens.home

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.preferences.BooleanPreferences
import com.w2sv.wifiwidget.preferences.WidgetPreferences
import com.w2sv.wifiwidget.ui.AppTheme

@Composable
fun PinAppWidgetButton(
    requestPinWidget: () -> Unit, launchLocationPermissionRequest: () -> Unit
) {
    var triggerOnClickListener by remember {
        mutableStateOf(false)
    }

    ElevatedButton(
        { triggerOnClickListener = true },
        modifier = Modifier.defaultMinSize(140.dp, 60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue_chill_dark)),
        content = {
            Text(
                text = stringResource(R.string.pin_widget),
            )
        }
    )

    if (triggerOnClickListener) {
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
                onClose = { triggerOnClickListener = false }
            )
        else {
            requestPinWidget()
            triggerOnClickListener = false
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

@Composable
@Preview(showSystemUi = true)
private fun LocationPermissionDialogPreview() {
    AppTheme {
        LocationPermissionDialog(
            onConfirm = { /*TODO*/ },
            onDismiss = { /*TODO*/ },
            onButtonPress = { /*TODO*/ }) {
        }
    }
}