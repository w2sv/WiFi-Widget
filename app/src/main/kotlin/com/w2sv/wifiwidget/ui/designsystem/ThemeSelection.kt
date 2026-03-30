package com.w2sv.wifiwidget.ui.designsystem

import androidx.annotation.StringRes
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.core.common.R
import com.w2sv.domain.model.Theme
import com.w2sv.wifiwidget.ui.util.then

@Preview
@Composable
private fun ThemeSelectionRowPrev() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ThemeSelectionRow(Theme.Default, {}, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun ThemeSelectionRow(
    selected: Theme,
    onSelected: (Theme) -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 44.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center
) {
    val indicators = remember { themeIndicators() }
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        indicators.forEach { properties ->
            ThemeIndicator(
                properties = properties,
                isSelected = { properties.theme == selected },
                onClick = { onSelected(properties.theme) },
                buttonModifier = Modifier.size(buttonSize)
            )
        }
    }
}

private fun themeIndicators(): List<ThemeIndicator> =
    listOf(
        ThemeIndicator(
            theme = Theme.Light,
            labelRes = R.string.light,
            color = ButtonColor.Uniform(Color.White)
        ),
        ThemeIndicator(
            theme = Theme.Default,
            labelRes = R.string.default_,
            color = ButtonColor.Gradient(
                Brush.linearGradient(
                    0.5f to Color.White,
                    0.5f to Color.Black
                )
            )
        ),
        ThemeIndicator(
            theme = Theme.Dark,
            labelRes = R.string.dark,
            color = ButtonColor.Uniform(Color.Black)
        )
    )

@Immutable
private data class ThemeIndicator(val theme: Theme, @StringRes val labelRes: Int, val color: ButtonColor)

@Immutable
private sealed interface ButtonColor {

    @Immutable
    @JvmInline
    value class Uniform(val containerColor: Color) : ButtonColor

    @Immutable
    @JvmInline
    value class Gradient(val brush: Brush) : ButtonColor
}

@Composable
private fun ThemeIndicator(
    properties: ThemeIndicator,
    isSelected: () -> Boolean,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = properties.labelRes),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        ThemeButton(
            buttonColor = properties.color,
            contentDescription = stringResource(id = R.string.theme_button_cd).format(
                stringResource(id = properties.labelRes)
            ),
            onClick = onClick,
            isSelected = isSelected,
            modifier = buttonModifier
        )
    }
}

@Composable
private fun ThemeButton(
    buttonColor: ButtonColor,
    contentDescription: String,
    onClick: () -> Unit,
    isSelected: () -> Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val border = animatedThemeButtonBorder(isSelected())

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then {
                when (buttonColor) {
                    is ButtonColor.Gradient -> background(buttonColor.brush)
                    is ButtonColor.Uniform -> background(buttonColor.containerColor)
                }
            }
            .border(border, CircleShape)
            .clickable(onClick = onClick, onClickLabel = contentDescription)
    )
}

@Composable
private fun animatedThemeButtonBorder(isSelected: Boolean): BorderStroke {
    val transition = updateTransition(
        targetState = isSelected,
        label = "ThemeButtonBorder"
    )

    val borderWidth by transition.animateDp(
        transitionSpec = { selectionSpec() },
        label = "borderWidth"
    ) { if (it) 3.dp else Dp.Hairline }

    val borderColor by transition.animateColor(
        transitionSpec = { selectionSpec() },
        label = "borderColor"
    ) {
        if (it) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    return BorderStroke(borderWidth, borderColor)
}

private fun <T> Transition.Segment<Boolean>.selectionSpec(): FiniteAnimationSpec<T> =
    if (targetState) {
        tween(
            durationMillis = BORDER_ANIMATION_DURATION,
            easing = Easing.Overshoot
        )
    } else {
        tween(durationMillis = BORDER_ANIMATION_DURATION)
    }

private const val BORDER_ANIMATION_DURATION = 500
