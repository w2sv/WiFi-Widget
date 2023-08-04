package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.wifiproperties

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.androidutils.generic.openUrlWithActivityNotFoundHandling
import com.w2sv.data.model.WifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.DialogButton
import com.w2sv.wifiwidget.ui.components.InfoDialog
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.theme.AppTheme

@Preview
@Composable
private fun Prev() {
    AppTheme {
        WifiPropertyInfoDialog(
            property = WifiProperty.SSID,
            onDismissRequest = {}
        )
    }
}

@Composable
internal fun WifiPropertyInfoDialog(
    property: WifiProperty,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stringArray = stringArrayResource(id = property.viewData.arrayRes)

    InfoDialog(
        modifier = modifier,
        title = stringResource(id = property.viewData.labelRes),
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