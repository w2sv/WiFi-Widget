package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.DialogButton
import com.w2sv.wifiwidget.ui.components.JostText

@Composable
fun ButtonRow(
    onCancel: () -> Unit,
    onApply: () -> Unit,
    applyButtonEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DialogButton(onClick = onCancel) {
            JostText(text = stringResource(R.string.cancel))
        }
        DialogButton(onClick = onApply, enabled = applyButtonEnabled) {
            JostText(text = stringResource(R.string.apply))
        }
    }
}