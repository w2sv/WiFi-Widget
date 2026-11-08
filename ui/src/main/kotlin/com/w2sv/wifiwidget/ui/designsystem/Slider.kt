package com.w2sv.wifiwidget.ui.designsystem

import androidx.annotation.IntRange
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

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
    val displayedValue = remember(value, makeLabel) { makeLabel(value) }

    Column(modifier = modifier) {
        Text(
            text = displayedValue,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        @Suppress("DEPRECATION")
        Slider(
            value = value,
            onValueChange = onValueChanged,
            steps = steps,
            valueRange = valueRange,
            modifier = Modifier.semantics {
                this.contentDescription = contentDescription
                stateDescription = displayedValue
            }
        )
    }
}
