package com.w2sv.wifiwidget.ui.shared

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.wifiwidget.R

@Composable
fun ThemeSelectionRow(
    modifier: Modifier = Modifier,
    selected: () -> Int,
    onSelected: (Int) -> Unit
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        remember {
            listOf(
                ThemeIndicatorProperties(
                    label = R.string.light,
                    buttonColoring = ButtonColoring.Uniform(Color.White)
                ),
                ThemeIndicatorProperties(
                    label = R.string.device_default,
                    ButtonColoring.Gradient(
                        Brush.linearGradient(
                            0.5f to Color.White,
                            0.5f to Color.Black,
                        )
                    )
                ),
                ThemeIndicatorProperties(
                    label = R.string.dark,
                    buttonColoring = ButtonColoring.Uniform(Color.Black)
                )
            )
        }
            .forEachIndexed { index, properties ->
                ThemeIndicator(
                    properties = properties,
                    isSelected = { index == selected() },
                    modifier = Modifier.padding(
                        horizontal = 16.dp
                    )
                ) {
                    onSelected(index)
                }
            }
    }
}

@Stable
private data class ThemeIndicatorProperties(
    @StringRes val label: Int,
    val buttonColoring: ButtonColoring
)

sealed class ButtonColoring {
    class Uniform(val color: Color) : ButtonColoring()
    class Gradient(val brush: Brush) : ButtonColoring()
}

@Composable
private fun ThemeIndicator(
    properties: ThemeIndicatorProperties,
    isSelected: () -> Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        JostText(
            text = stringResource(id = properties.label),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.margin_minimal))
        )
        ThemeIndicatorButton(
            buttonColoring = properties.buttonColoring,
            onClick = onClick,
            size = 36.dp,
            isSelected = isSelected
        )
    }
}

@Composable
fun ThemeIndicatorButton(
    buttonColoring: ButtonColoring,
    onClick: () -> Unit,
    size: Dp,
    isSelected: () -> Boolean
) {
    val modifier = Modifier.size(size)

    when (buttonColoring) {
        is ButtonColoring.Uniform -> Unit // Button(
//            onClick,
//            modifier = modifier,
//            shape = CircleShape,
//            colors = ButtonDefaults.elevatedButtonColors(containerColor = buttonColoring.color),
//            border = border
//        ) {}

        is ButtonColoring.Gradient -> CircleGradientButton(
            onClick,
            modifier = modifier,
            size = size,
            brush = buttonColoring.brush,
            isSelected = isSelected
        )
    }
}

@Composable
private fun CircleGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp,
    brush: Brush,
    isSelected: () -> Boolean
) {
    val radius = with(LocalDensity.current) { (size / 2).toPx() }
    val primaryColor = MaterialTheme.colorScheme.primary

    Button(
        modifier = modifier
            .drawBehind {
                drawCircle(
                    brush,
                    radius = radius
                )
                if (isSelected()){
                    drawCircle(
                        primaryColor,
                        radius = radius + 1,
                        style = Stroke(3f)
                    )
                }
            },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        onClick = onClick,
        shape = CircleShape
    ) {}
}

@Preview
@Composable
fun Prev() {
    WifiWidgetTheme {
        CircleGradientButton(
            {},
            Modifier.size(32.dp),
            32.dp,
            Brush.linearGradient(
                0.5f to Color.White,
                0.5f to Color.Black,
            ),
            { true }
        )
    }
}