package com.w2sv.wifiwidget.ui.designsystem

import androidx.annotation.IntRange
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun SliderRow(
    label: String,
    slider: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        KeyboardArrowRightIcon(modifier = Modifier.padding(end = 8.dp))
        Text(label, modifier = Modifier.weight(0.4f), maxLines = 2)
        Box(modifier = Modifier.weight(0.6f), contentAlignment = Alignment.Center) {
            slider()
        }
    }
}

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
            text = remember(value, makeLabel) {
                makeLabel(value)
            },
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Slider(
            value = value,
            onValueChange = onValueChanged,
            modifier = Modifier
                .semantics {
                    this.contentDescription = contentDescription
                },
            steps = steps,
            valueRange = valueRange
        )
    }
}