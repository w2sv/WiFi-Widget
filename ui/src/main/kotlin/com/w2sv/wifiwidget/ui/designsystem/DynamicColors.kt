package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.w2sv.core.common.R

@Composable
fun ConfigureUseDynamicColors(
    useDynamicColors: Boolean,
    toggleDynamicColors: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    below: BoxScopeComposable? = null
) {
    TLayout(
        central = {
            Text(
                text = stringResource(R.string.dynamic_colors),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = modifier,
        trailing = {
            Switch(
                checked = useDynamicColors,
                onCheckedChange = { toggleDynamicColors(it) }
            )
        },
        below = below
    )
}
