package com.w2sv.wifiwidget.ui.shared

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.Theme
import com.w2sv.wifiwidget.R

@Composable
fun ThemeSelectionRow(
    modifier: Modifier = Modifier,
    selected: () -> Theme,
    onSelected: (Theme) -> Unit
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        remember {
            listOf(
                ThemeIndicatorProperties(
                    theme = Theme.Light,
                    label = R.string.light,
                    buttonColoring = ButtonColoring.Uniform(Color.White)
                ),
                ThemeIndicatorProperties(
                    theme = Theme.DeviceDefault,
                    label = R.string.device_default,
                    buttonColoring = ButtonColoring.Gradient(
                        Brush.linearGradient(
                            0.5f to Color.White,
                            0.5f to Color.Black,
                        )
                    )
                ),
                ThemeIndicatorProperties(
                    theme = Theme.Dark,
                    label = R.string.dark,
                    buttonColoring = ButtonColoring.Uniform(Color.Black)
                ),
                ThemeIndicatorProperties(
                    theme = Theme.Custom,
                    label = R.string.custom,
                    buttonColoring = ButtonColoring.Gradient(
                        Brush.linearGradient(
                            0.5f to Color.Magenta,
                            0.5f to Color.Cyan,
                        )
                    )
                )
            )
        }
            .forEach { properties ->
                ThemeIndicator(
                    properties = properties,
                    isSelected = { properties.theme == selected() },
                    modifier = Modifier.padding(
                        horizontal = 16.dp
                    )
                ) {
                    onSelected(properties.theme)
                }
            }
    }
}

@Stable
private data class ThemeIndicatorProperties(
    val theme: Theme,
    @StringRes val label: Int,
    val buttonColoring: ButtonColoring
)

sealed class ButtonColoring(val containerColor: Color) {
    class Uniform(color: Color) : ButtonColoring(color)
    class Gradient(val brush: Brush) : ButtonColoring(Color.Transparent)
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
        ThemeButton(
            buttonColoring = properties.buttonColoring,
            onClick = onClick,
            size = 36.dp,
            isSelected = isSelected
        )
    }
}

@Composable
fun ThemeButton(
    buttonColoring: ButtonColoring,
    onClick: () -> Unit,
    size: Dp,
    isSelected: () -> Boolean,
    modifier: Modifier = Modifier
) {
    val radius = with(LocalDensity.current) { (size / 2).toPx() }

    Button(
        modifier = modifier
            .size(size)
            .drawBehind {
                if (buttonColoring is ButtonColoring.Gradient) {
                    drawCircle(
                        buttonColoring.brush,
                        radius = radius
                    )
                }
            },
        colors = ButtonDefaults.buttonColors(containerColor = buttonColoring.containerColor),
        onClick = onClick,
        shape = CircleShape,
        border = when (isSelected()) {
            true -> BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
            false -> null
        }
    ) {}
}

@Preview
@Composable
fun Prev() {
    WifiWidgetTheme {
        ThemeButton(
            ButtonColoring.Gradient(
                Brush.linearGradient(
                    0.5f to Color.White,
                    0.5f to Color.Black,
                )
            ),
            {},
            32.dp,
            { true }
        )
    }
}