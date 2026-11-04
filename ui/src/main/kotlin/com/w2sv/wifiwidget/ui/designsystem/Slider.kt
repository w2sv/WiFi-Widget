package com.w2sv.wifiwidget.ui.designsystem

import androidx.annotation.IntRange
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.w2sv.wifiwidget.ui.util.contentDescription

@Composable
fun SliderWithLabel(
    value: Float,
    @IntRange(from = 0) steps: Int,
    makeLabel: (Float) -> String,
    onValueChanged: (Float) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    Column(modifier = modifier) {
        Text(
            text = remember(value, makeLabel) { makeLabel(value) },
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Slider(
            state = rememberSliderState(
                value = value,
                steps = steps,
                trackRange = valueRange
            ),
            onValueChange = onValueChanged,
            modifier = Modifier.contentDescription(contentDescription)
        )
    }
}
