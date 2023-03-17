package com.w2sv.wifiwidget.ui.shared

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
                    buttonType = ButtonType.Default(Color.White)
                ),
                ThemeIndicatorProperties(
                    label = R.string.device_default,
                    ButtonType.DeviceDefault
                ),
                ThemeIndicatorProperties(
                    label = R.string.dark,
                    buttonType = ButtonType.Default(Color.Black)
                )
            )
        }
            .forEachIndexed { index, properties ->
                ThemeIndicator(
                    properties = properties,
                    selected = index == selected(),
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
    val buttonType: ButtonType
)

sealed class ButtonType {
    class Default(val color: Color) : ButtonType()
    object DeviceDefault : ButtonType()
}

@Composable
private fun ThemeIndicator(
    properties: ThemeIndicatorProperties,
    selected: Boolean,
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
            buttonType = properties.buttonType,
            onClick = onClick,
            size = 36.dp,
            shape = CircleShape,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            border = if (selected)
                BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
            else
                null
        )
    }
}

@Composable
fun ThemeIndicatorButton(
    buttonType: ButtonType,
    onClick: () -> Unit,
    size: Dp,
    shape: Shape,
    elevation: ButtonElevation,
    border: BorderStroke?
) {
    val modifier = Modifier.size(size)

    when (buttonType) {
        is ButtonType.Default -> ElevatedButton(
            onClick,
            modifier = modifier,
            shape = shape,
            elevation = elevation,
            colors = ButtonDefaults.elevatedButtonColors(containerColor = buttonType.color),
            border = border
        ) {}

        is ButtonType.DeviceDefault -> DeviceDefaultButton(
            onClick,
            modifier = modifier,
            size = size,
            shape = shape,
            border = border
        )
    }
}

@Composable
private fun DeviceDefaultButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp,
    shape: Shape,
    border: BorderStroke?
) {
    val radius = with(LocalDensity.current) { (size / 2).toPx() }

    OutlinedButton(
        modifier = modifier
            .drawBehind {
                drawCircle(
                    Brush.linearGradient(
                        0.5f to Color.White,
                        0.5f to Color.Black,
                    ),
                    radius = radius
                )
            },
        onClick = onClick,
        border = border,
        shape = shape
    ) {}
}

@Preview
@Composable
fun Prev() {
    WifiWidgetTheme {
        DeviceDefaultButton(
            {},
            Modifier.size(32.dp),
            32.dp,
            CircleShape,
            BorderStroke(Dp.Hairline, MaterialTheme.colorScheme.primary)
        )
    }
}