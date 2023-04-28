package com.w2sv.wifiwidget.ui.screens.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.w2sv.common.enums.Theme
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.shared.DialogButton
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme

@Preview
@Composable
private fun Prev() {
    WifiWidgetTheme {
        ThemeSelectionDialog(
            onDismissRequest = { /*TODO*/ },
            selectedTheme = { Theme.DeviceDefault },
            onThemeSelected = {},
            applyButtonEnabled = { true },
            onApplyButtonClick = {}
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    onDismissRequest: () -> Unit,
    selectedTheme: () -> Theme,
    onThemeSelected: (Theme) -> Unit,
    applyButtonEnabled: () -> Boolean,
    onApplyButtonClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { JostText(text = stringResource(id = R.string.theme)) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_nightlight_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        confirmButton = {
            DialogButton(onClick = { onApplyButtonClick() }, enabled = applyButtonEnabled()) {
                JostText(text = stringResource(id = R.string.apply))
            }
        },
        dismissButton = {
            DialogButton(onClick = onDismissRequest) {
                JostText(text = stringResource(id = R.string.cancel))
            }
        },
        text = {
            ThemeSelectionRow(selected = selectedTheme, onSelected = onThemeSelected)
        }
    )
}