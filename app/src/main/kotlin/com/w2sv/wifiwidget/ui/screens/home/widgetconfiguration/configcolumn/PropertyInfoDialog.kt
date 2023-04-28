package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.configcolumn

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.common.enums.WifiProperty
import com.w2sv.common.extensions.openUrlWithActivityNotFoundHandling
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.shared.DialogButton
import com.w2sv.wifiwidget.ui.shared.InfoDialog
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme

@Preview
@Composable
private fun Prev() {
    WifiWidgetTheme {
        PropertyInfoDialog(
            WifiProperty.SSID,
            onDismissRequest = {}
        )
    }
}

@Composable
internal fun PropertyInfoDialog(
    property: WifiProperty,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val stringArray = stringArrayResource(id = property.infoStringArrayRes)

    InfoDialog(
        modifier = modifier,
        title = stringResource(id = property.labelRes),
        text = stringArray[0],
        learnMoreButton = when (stringArray[1].isNotEmpty()) {
            true -> {
                {
                    DialogButton(
                        onClick = {
                            context.openUrlWithActivityNotFoundHandling(stringArray[1])
                            onDismissRequest()
                        },
                        modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
                    ) {
                        JostText(text = stringResource(R.string.learn_more))
                    }
                }
            }

            false -> null
        },
        onDismissRequest = onDismissRequest
    )
}