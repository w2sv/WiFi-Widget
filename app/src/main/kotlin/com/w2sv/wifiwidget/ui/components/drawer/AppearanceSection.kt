package com.w2sv.wifiwidget.ui.components.drawer

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.data.model.Theme
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.ThemeSelectionRow

val dynamicColorsSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
internal fun AppearanceSection(
    useDynamicColors: Boolean,
    onToggleDynamicColors: (Boolean) -> Unit,
    selectedTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (dynamicColorsSupported) {
            UseDynamicColorsRow(
                useDynamicColors = useDynamicColors,
                onToggleDynamicColors = onToggleDynamicColors
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            JostText(
                text = stringResource(id = R.string.theme),
                color = MaterialTheme.colorScheme.onSurface
            )
            ThemeSelectionRow(
                selected = selectedTheme,
                onSelected = onThemeSelected,
                modifier = Modifier
                    .width(180.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            )
        }
    }
}

@Composable
fun UseDynamicColorsRow(
    useDynamicColors: Boolean,
    onToggleDynamicColors: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        JostText(
            text = stringResource(R.string.use_dynamic_colors),
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = useDynamicColors,
            onCheckedChange = {
                onToggleDynamicColors(
                    it
                )
            }
        )
    }
}