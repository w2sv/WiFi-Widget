package com.w2sv.wifiwidget.screens.home

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.preferences.BooleanPreferences
import com.w2sv.wifiwidget.ui.AppTheme

@Composable
internal fun PinAppWidgetButton(
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
        if (!BooleanPreferences.locationPermissionDialogShown)
            LocationPermissionDialog(
                onConfirm = {
                    launchLocationPermissionRequest()
                },
                onButtonPress = {
                    BooleanPreferences.locationPermissionDialogShown = true
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
    onButtonPress: () -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Information Icon",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "SSID Display requires Location Access",
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            ElevatedButton({
                onConfirm()
                onButtonPress()
                onClose()
            }) { Text(text = "Got it!") }
        },
        onDismissRequest = onClose,
        text = {
            Text(
                text = buildAnnotatedString {
                    append("If you want your SSID to be displayed amongst the widget properties, you'll have to ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("grant the app location access")
                    }
                    append(". Furthermore, you'll have to ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(
                            "enable your location service.\n\n" +
                                    "This is entirely optional, so feel free to decline."
                        )
                    }
                }
            )
        }
    )
}

@Composable
@Preview(showSystemUi = true)
private fun LocationPermissionDialogPreview() {
    AppTheme {
        LocationPermissionDialog(
            onConfirm = { /*TODO*/ },
            onButtonPress = { /*TODO*/ }) {
        }
    }
}