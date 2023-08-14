package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.content.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.JostText
import kotlin.math.roundToInt

@Composable
internal fun OpacitySliderWithLabel(
    opacity: Float,
    onOpacityChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(modifier = modifier) {
        JostText(
            text = "${(opacity * 100).roundToInt()}%",
            color = MaterialTheme.colorScheme.onSurface,
            modifier = modifier.align(Alignment.CenterHorizontally)
        )
        Slider(
            value = opacity,
            onValueChange = onOpacityChanged,
            modifier = Modifier
                .semantics {
                    contentDescription = context.getString(
                        R.string.opacity_slider_cd
                    )
                },
            steps = 9
        )
    }
}