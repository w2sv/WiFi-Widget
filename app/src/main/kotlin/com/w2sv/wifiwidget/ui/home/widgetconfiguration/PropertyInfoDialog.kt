package com.w2sv.wifiwidget.ui.home.widgetconfiguration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.androidutils.extensions.getNestedStringArray
import com.w2sv.androidutils.extensions.openUrl
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.DialogButton
import com.w2sv.wifiwidget.ui.JostText
import com.w2sv.wifiwidget.ui.WifiWidgetTheme

@Composable
fun PropertyInfoDialog(
    propertyIndex: Int,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    with(context.resources.getNestedStringArray(R.array.wifi_property_data_arrays, propertyIndex)) {
        StatelessPropertyInfoDialog(
            modifier = modifier,
            title = get(0),
            text = get(1),
            learnMoreButtonOnClickListener = {
                context.openUrl(get(2))
                onDismissRequest()
            },
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
private fun StatelessPropertyInfoDialog(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    learnMoreButtonOnClickListener: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(onClick = onDismissRequest) {
                JostText(text = stringResource(R.string.close))
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
                DialogButton(
                    onClick = learnMoreButtonOnClickListener,
                    modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
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
        StatelessPropertyInfoDialog(
            Modifier,
            "SSID",
            "Service Set Identifier. Your network's name.",
            {},
            {})
    }
}