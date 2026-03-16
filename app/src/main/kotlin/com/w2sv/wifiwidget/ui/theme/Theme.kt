package com.w2sv.wifiwidget.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.SystemBarStyle
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import com.w2sv.wifiwidget.R

@SuppressLint("NewApi")
@Composable
fun AppTheme(
    useDarkTheme: Boolean = false,
    useAmoledBlackTheme: Boolean = false,
    useDynamicColors: Boolean = false,
    setSystemBarStyles: (SystemBarStyle, SystemBarStyle) -> Unit = { _, _ -> },
    context: Context = LocalContext.current,
    content: @Composable () -> Unit
) {
    // Reset system bar style on useDarkTheme change
    LaunchedEffect(useDarkTheme) {
        val systemBarStyle = if (useDarkTheme) {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        }

        setSystemBarStyles(
            systemBarStyle,
            systemBarStyle
        )
    }

    val colorScheme = when {
        useDynamicColors && useDarkTheme && useAmoledBlackTheme -> dynamicDarkColorScheme(context).amoledBlack()
        useDynamicColors && useDarkTheme -> dynamicDarkColorScheme(context)
        useDynamicColors && !useDarkTheme -> dynamicLightColorScheme(context)
        useDarkTheme && useAmoledBlackTheme -> darkColors.amoledBlack()
        useDarkTheme -> darkColors
        else -> lightColors
    }

    MaterialTheme(
        colorScheme = colorScheme.animate(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
        typography = typography,
        content = content
    )
}

private fun ColorScheme.amoledBlack(): ColorScheme =
    copy(background = Color.Black, surface = Color.Black, onBackground = Color.White, onSurface = Color.White)

@Composable
private fun ColorScheme.animate(animationSpec: AnimationSpec<Color>): ColorScheme =
    copy(
        primary = primary.animate(animationSpec),
        primaryContainer = primaryContainer.animate(animationSpec),
        secondary = secondary.animate(animationSpec),
        secondaryContainer = secondaryContainer.animate(animationSpec),
        tertiary = tertiary.animate(animationSpec),
        tertiaryContainer = tertiaryContainer.animate(animationSpec),
        background = background.animate(animationSpec),
        surface = surface.animate(animationSpec),
        surfaceTint = surfaceTint.animate(animationSpec),
        surfaceBright = surfaceBright.animate(animationSpec),
        surfaceDim = surfaceDim.animate(animationSpec),
        surfaceContainer = surfaceContainer.animate(animationSpec),
        surfaceContainerHigh = surfaceContainerHigh.animate(animationSpec),
        surfaceContainerHighest = surfaceContainerHighest.animate(animationSpec),
        surfaceContainerLow = surfaceContainerLow.animate(animationSpec),
        surfaceContainerLowest = surfaceContainerLowest.animate(animationSpec),
        surfaceVariant = surfaceVariant.animate(animationSpec),
        error = error.animate(animationSpec),
        errorContainer = errorContainer.animate(animationSpec),
        onPrimary = onPrimary.animate(animationSpec),
        onPrimaryContainer = onPrimaryContainer.animate(animationSpec),
        onSecondary = onSecondary.animate(animationSpec),
        onSecondaryContainer = onSecondaryContainer.animate(animationSpec),
        onTertiary = onTertiary.animate(animationSpec),
        onTertiaryContainer = onTertiaryContainer.animate(animationSpec),
        onBackground = onBackground.animate(animationSpec),
        onSurface = onSurface.animate(animationSpec),
        onSurfaceVariant = onSurfaceVariant.animate(animationSpec),
        onError = onError.animate(animationSpec),
        onErrorContainer = onErrorContainer.animate(animationSpec),
        inversePrimary = inversePrimary.animate(animationSpec),
        inverseSurface = inverseSurface.animate(animationSpec),
        inverseOnSurface = inverseOnSurface.animate(animationSpec),
        outline = outline.animate(animationSpec),
        outlineVariant = outlineVariant.animate(animationSpec),
        scrim = scrim.animate(animationSpec)
    )

@Composable
private fun Color.animate(animationSpec: AnimationSpec<Color>): Color =
    animateColorAsState(this, animationSpec).value

private val defaultTypography = Typography()
private val jost = Font(R.font.jost).toFontFamily()

private val typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = jost),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = jost),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = jost),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = jost),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = jost),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = jost),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = jost),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = jost),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = jost),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = jost),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = jost),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = jost),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = jost),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = jost),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = jost)
)
