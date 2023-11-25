package com.w2sv.wifiwidget.ui.components.drawer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.domain.model.Theme
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppFontText
import com.w2sv.wifiwidget.ui.components.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.components.UseDynamicColorsRow
import com.w2sv.wifiwidget.ui.components.dynamicColorsSupported

@Composable
internal fun AppearanceSection(
    useDynamicColors: Boolean,
    onToggleDynamicColors: (Boolean) -> Unit,
    selectedTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (dynamicColorsSupported) {
            UseDynamicColorsRow(
                useDynamicColors = useDynamicColors,
                onToggleDynamicColors = onToggleDynamicColors,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            AppFontText(
                text = stringResource(id = R.string.theme),
                color = MaterialTheme.colorScheme.onSurface,
            )
            ThemeSelectionRow(
                selected = selectedTheme,
                onSelected = onThemeSelected,
                modifier = Modifier
                    .width(200.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                themeIndicatorModifier = Modifier
                    .sizeIn(maxHeight = 92.dp, maxWidth = 42.dp),
            )
        }
    }
}
