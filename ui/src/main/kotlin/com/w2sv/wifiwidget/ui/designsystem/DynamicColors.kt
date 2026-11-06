package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.w2sv.core.resources.R

@Composable
fun ConfigureUseDynamicColors(
    useDynamicColors: Boolean,
    toggleDynamicColors: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    below: BoxScopeComposable? = null
) {
    val label = stringResource(R.string.dynamic_colors)
    TLayout(
        central = {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = modifier,
        trailing = {
            Switch(
                checked = useDynamicColors,
                onCheckedChange = { toggleDynamicColors(it) },
                modifier = Modifier.semantics { contentDescription = label }
            )
        },
        below = below
    )
}
