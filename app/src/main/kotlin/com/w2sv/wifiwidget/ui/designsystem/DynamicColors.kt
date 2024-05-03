package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.w2sv.wifiwidget.R

@Composable
fun UseDynamicColorsRow(
    useDynamicColors: Boolean,
    onToggleDynamicColors: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
    ) {
        leadingIcon?.invoke()
        Text(
            text = stringResource(R.string.use_dynamic_colors),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = useDynamicColors,
            onCheckedChange = {
                onToggleDynamicColors(
                    it,
                )
            },
        )
    }
}
