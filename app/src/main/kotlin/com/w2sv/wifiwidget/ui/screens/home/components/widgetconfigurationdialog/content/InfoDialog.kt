package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.content

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.androidutils.generic.openUrlWithActivityNotFoundHandling
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.DialogButton
import com.w2sv.wifiwidget.ui.components.InfoDialog
import com.w2sv.wifiwidget.ui.components.JostText

@Stable
data class InfoDialogData(
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int,
    val learnMoreUrl: String? = null
)

@Composable
internal fun InfoDialog(
    data: InfoDialogData,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    InfoDialog(
        modifier = modifier,
        title = stringResource(id = data.labelRes),
        text = stringResource(id = data.descriptionRes),
        learnMoreButton = data.learnMoreUrl?.let {
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