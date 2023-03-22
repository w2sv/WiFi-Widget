package com.w2sv.wifiwidget.ui.home.configurationdialog.content

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
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.extensions.openUrlWithActivityNotFoundHandling
import com.w2sv.wifiwidget.ui.shared.DialogButton
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme

@Preview
@Composable
private fun Prev() {
    WifiWidgetTheme {
        PropertyInfoDialog(
            Modifier,
            "SSID",
            "Service Set Identifier. Your network's name.",
            {},
            {}
        )
    }
}

@Composable
internal fun PropertyInfoDialog(
    propertyIndex: Int,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    with(context.resources.getNestedStringArray(R.array.wifi_property_data_arrays, propertyIndex)) {
        PropertyInfoDialog(
            modifier = modifier,
            title = get(0),
            text = get(1),
            learnMoreButtonOnClickListener = get(2).let {
                if (it.isNotEmpty()) {
                    {
                        context.openUrlWithActivityNotFoundHandling(it)
                        onDismissRequest()
                    }
                } else null
            },
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
private fun PropertyInfoDialog(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    learnMoreButtonOnClickListener: (() -> Unit)?,
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
                learnMoreButtonOnClickListener?.let {
                    DialogButton(
                        onClick = it,
                        modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
                    ) {
                        JostText(text = "Learn more")
                    }
                }
            }
        }
    )
}