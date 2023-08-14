package com.w2sv.wifiwidget.ui.screens.home.components.widget_configuration_dialog.content

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
import com.w2sv.wifiwidget.ui.screens.home.components.widget_configuration_dialog.model.PropertyInfoDialogData

@Composable
fun PropertyInfoDialog(
    data: PropertyInfoDialogData,
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