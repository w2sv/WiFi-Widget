package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.configcolumn

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.shared.JostText
import kotlin.math.roundToInt

@Composable
internal fun ColumnScope.OpacitySliderWithValue(
    modifier: Modifier = Modifier,
    opacity: () -> Float,
    onOpacityChanged: (Float) -> Unit
) {
    val context = LocalContext.current

    JostText(
        text = "${(opacity() * 100).roundToInt()}%",
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.align(Alignment.CenterHorizontally)
    )
    Slider(
        value = opacity(),
        onValueChange = onOpacityChanged,
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .semantics {
                contentDescription = context.getString(
                    R.string.set_the_widget_background_opacity
                )
            },
        steps = 9
    )
}