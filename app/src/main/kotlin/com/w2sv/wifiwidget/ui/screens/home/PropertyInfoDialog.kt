package com.w2sv.wifiwidget.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.androidutils.extensions.goToWebpage
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.JostText
import com.w2sv.wifiwidget.ui.WifiWidgetTheme
import com.w2sv.wifiwidget.utils.getNestedStringArray

@Composable
fun PropertyInfoDialog(
    propertyIndex: Int,
    onDismissRequest: () -> Unit
) {
    with(LocalContext.current) {
        with(resources.getNestedStringArray(R.array.wifi_property_data_arrays, propertyIndex)) {
            PropertyInfoDialog(
                title = get(0),
                text = get(1),
                learnMoreButtonOnClickListener = {
                    goToWebpage(get(2))
                    onDismissRequest()
                },
                onDismissRequest = onDismissRequest
            )
        }
    }
}

@Composable
private fun PropertyInfoDialog(
    title: String,
    text: String,
    learnMoreButtonOnClickListener: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            ElevatedButton(
                onClick = onDismissRequest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.elevatedButtonElevation(8.dp)
            ) {
                JostText(text = "Close")
            }
        },
        title = {
            JostText(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                Modifier
                    .sizeIn(maxHeight = 520.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                Arrangement.Center,
                Alignment.CenterHorizontally
            ) {
                JostText(text = text, textAlign = TextAlign.Center)
                ElevatedButton(
                    onClick = learnMoreButtonOnClickListener,
                    modifier = Modifier.padding(top = 20.dp, bottom = 12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.elevatedButtonElevation(8.dp)
                ) {
                    JostText(text = "Learn more")
                }
            }
        }
    )
}

@Preview
@Composable
private fun Preview() {
    WifiWidgetTheme {
        PropertyInfoDialog("SSID", "Service Set Identifier. Your network's name.", {}, {})
    }
}