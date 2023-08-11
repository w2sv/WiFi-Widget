package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.w2sv.data.model.WifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.DialogButton
import com.w2sv.wifiwidget.ui.components.InfoIcon
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.theme.AppTheme

sealed interface LAPRequestTrigger {
    data object PinWidgetButtonPress : LAPRequestTrigger
    class PropertyCheckChange(val property: WifiProperty) : LAPRequestTrigger
}

@Composable
fun LocationAccessPermissionRationalDialog(
    onProceed: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        icon = {
            InfoIcon()
        },
        text = {
            JostText(
                text = stringResource(id = R.string.lap_dialog_text),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            DialogButton(
                onClick = onProceed,
                modifier = Modifier.fillMaxWidth()
            ) { JostText(text = stringResource(R.string.understood)) }
        },
        onDismissRequest = onProceed
    )
}


@Preview
@Composable
private fun Prev() {
    AppTheme {
        LocationAccessPermissionRationalDialog({})
    }
}