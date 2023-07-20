package com.w2sv.wifiwidget.ui.components

import android.view.animation.OvershootInterpolator
import androidx.annotation.StringRes
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.enums.Theme
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.utils.toEasing

@Composable
fun ThemeSelectionRow(
    modifier: Modifier = Modifier,
    customThemeIndicatorProperties: ThemeIndicatorProperties? = null,
    selected: Theme,
    onSelected: (Theme) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        buildList {
            add(
                ThemeIndicatorProperties(
                    theme = Theme.Light,
                    label = R.string.light,
                    buttonColoring = ButtonColor.Uniform(Color.White)
                )
            )
            add(
                ThemeIndicatorProperties(
                    theme = Theme.DeviceDefault,
                    label = R.string.device_default,
                    buttonColoring = ButtonColor.Gradient(
                        Brush.linearGradient(
                            0.5f to Color.White,
                            0.5f to Color.Black,
                        )
                    )
                )
            )
            add(
                ThemeIndicatorProperties(
                    theme = Theme.Dark,
                    label = R.string.dark,
                    buttonColoring = ButtonColor.Uniform(Color.Black)
                )
            )
            customThemeIndicatorProperties?.let {
                add(it)
            }
        }
            .forEach { properties ->
                ThemeIndicator(
                    properties = properties,
                    isSelected = { properties.theme == selected },
                    modifier = Modifier.padding(
                        horizontal = 12.dp
                    )
                ) {
                    onSelected(properties.theme)
                }
            }
    }
}

@Stable
data class ThemeIndicatorProperties(
    val theme: Theme,
    @StringRes val label: Int,
    val buttonColoring: ButtonColor
)

sealed class ButtonColor(val containerColor: Color) {
    class Uniform(color: Color) : ButtonColor(color)
    class Gradient(val brush: Brush) : ButtonColor(Color.Transparent)
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
            buttonColor = properties.buttonColoring,
            contentDescription = stringResource(id = R.string.theme_button_cd).format(
                stringResource(id = properties.label)
            ),
            onClick = onClick,
            size = 36.dp,
            isSelected = isSelected
        )
    }
}

@Composable
fun ThemeButton(
    buttonColor: ButtonColor,
    contentDescription: String,
    onClick: () -> Unit,
    size: Dp,
    isSelected: () -> Boolean,
    modifier: Modifier = Modifier
) {
    val radius = with(LocalDensity.current) { (size / 2).toPx() }

    val transition = updateTransition(targetState = isSelected(), label = "")

    val borderWidth by transition.animateDp(
        transitionSpec = {
            if (targetState) {
                tween(
                    durationMillis = BORDER_ANIMATION_DURATION,
                    easing = OvershootInterpolator().toEasing()
                )
            } else {
                tween(durationMillis = BORDER_ANIMATION_DURATION)
            }
        }, label = ""
    ) { state ->
        if (state) 3.dp else 0.dp
    }

    val borderColor by transition.animateColor(
        transitionSpec = {
            if (targetState) {
                tween(
                    durationMillis = BORDER_ANIMATION_DURATION,
                    easing = OvershootInterpolator().toEasing()
                )
            } else {
                tween(durationMillis = BORDER_ANIMATION_DURATION)
            }
        }, label = ""
    ) { state ->
        if (state) MaterialTheme.colorScheme.primary else Color.Transparent
    }

    Button(
        modifier = modifier
            .semantics {
                this.contentDescription = contentDescription
            }
            .size(size)
            .drawBehind {
                if (buttonColor is ButtonColor.Gradient) {
                    drawCircle(
                        buttonColor.brush,
                        radius = radius
                    )
                }
            },
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor.containerColor),
        onClick = onClick,
        shape = CircleShape,
        border = BorderStroke(borderWidth, borderColor)
    ) {}
}

private const val BORDER_ANIMATION_DURATION = 500

@Preview
@Composable
fun Prev() {
    AppTheme {
        ThemeButton(
            ButtonColor.Gradient(
                Brush.linearGradient(
                    0.5f to Color.White,
                    0.5f to Color.Black,
                )
            ),
            "Device Default",
            {},
            32.dp,
            { true }
        )
    }
}