package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.configcolumn

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.androidutils.extensions.getNestedStringArray
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.extensions.openUrlWithActivityNotFoundHandling
import com.w2sv.wifiwidget.ui.shared.DialogButton
import com.w2sv.wifiwidget.ui.shared.InfoDialog
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme

@Preview
@Composable
private fun Prev() {
    WifiWidgetTheme {
        PropertyInfoDialog(
            1,
            onDismissRequest = {}
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
        InfoDialog(
            modifier = modifier,
            title = get(0),
            text = get(1),
            learnMoreButton = get(2).let {
                if (it.isNotEmpty()) {
                    {
                        DialogButton(
                            onClick = {
                                context.openUrlWithActivityNotFoundHandling(it)
                                onDismissRequest()
                            },
                            modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
                        ) {
                            JostText(text = "Learn more")
                        }
                    }
                } else null
            },
            onDismissRequest = onDismissRequest
        )
    }
}