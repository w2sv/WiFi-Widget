package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.wifiproperties

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.androidutils.generic.openUrlWithActivityNotFoundHandling
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.DialogButton
import com.w2sv.wifiwidget.ui.components.InfoDialog
import com.w2sv.wifiwidget.ui.components.JostText

@Composable
internal fun InfoDialog(
    @StringRes labelRes: Int,
    @StringRes descriptionRes: Int,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    learnMoreUrl: String? = null
) {
    val context = LocalContext.current

    InfoDialog(
        modifier = modifier,
        title = stringResource(id = labelRes),
        text = stringResource(id = descriptionRes),
        learnMoreButton = learnMoreUrl?.let {
            {
                DialogButton(
                    onClick = {
                        context.openUrlWithActivityNotFoundHandling(it)
                        onDismissRequest()
                    },
                    modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
                ) {
                    JostText(text = stringResource(R.string.learn_more))
                }
            }
        },
        onDismissRequest = onDismissRequest
    )
}