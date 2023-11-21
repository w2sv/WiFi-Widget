package com.w2sv.wifiwidget.ui.components

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.w2sv.wifiwidget.R

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
val dynamicColorsSupported
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
fun UseDynamicColorsRow(
    useDynamicColors: Boolean,
    onToggleDynamicColors: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
    ) {
        AppFontText(
            text = stringResource(R.string.use_dynamic_colors),
            color = MaterialTheme.colorScheme.onSurface,
        )
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
