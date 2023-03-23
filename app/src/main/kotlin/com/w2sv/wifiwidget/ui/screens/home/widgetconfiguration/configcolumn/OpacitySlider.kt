package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.configcolumn

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w2sv.wifiwidget.ui.shared.JostText
import kotlin.math.roundToInt

@Composable
internal fun ColumnScope.OpacitySliderWithValue(
    modifier: Modifier = Modifier,
    opacity: () -> Float,
    onOpacityChanged: (Float) -> Unit
) {
    JostText(
        text = "${(opacity() * 100).roundToInt()}%",
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.align(Alignment.CenterHorizontally)
    )
    Slider(
        value = opacity(),
        onValueChange = onOpacityChanged,
        modifier = Modifier.padding(horizontal = 32.dp),
        steps = 9
    )
}