package com.w2sv.wifiwidget.ui.components.navigationdrawer

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.data.model.Theme
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.DialogButton
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.theme.AppTheme

@Preview
@Composable
private fun Prev() {
    AppTheme {
        ThemeSelectionDialog(
            onDismissRequest = { /*TODO*/ },
            useDynamicTheme = true,
            onToggleDynamicTheme = {},
            selectedTheme = Theme.DeviceDefault,
            onThemeSelected = {},
            applyButtonEnabled = true,
            onApplyButtonClick = {}
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    onDismissRequest: () -> Unit,
    useDynamicTheme: Boolean,
    onToggleDynamicTheme: (Boolean) -> Unit,
    selectedTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
    applyButtonEnabled: Boolean,
    onApplyButtonClick: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.width(280.dp),
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
            DialogButton(onClick = { onApplyButtonClick() }, enabled = applyButtonEnabled) {
                JostText(text = stringResource(id = R.string.apply))
            }
        },
        dismissButton = {
            DialogButton(onClick = onDismissRequest) {
                JostText(text = stringResource(id = R.string.cancel))
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        JostText(text = "Use dynamic theme", color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = useDynamicTheme,
                            onCheckedChange = {
                                onToggleDynamicTheme(
                                    it
                                )
                            }
                        )
                    }
                }
                ThemeSelectionRow(
                    selected = selectedTheme,
                    onSelected = onThemeSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
    )
}