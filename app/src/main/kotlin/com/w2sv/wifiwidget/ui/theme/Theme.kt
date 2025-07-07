package com.w2sv.wifiwidget.ui.theme

import android.annotation.SuppressLint
import androidx.activity.SystemBarStyle
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.w2sv.wifiwidget.ui.LocalUseDarkTheme

@SuppressLint("NewApi")
@Composable
fun AppTheme(
    useDarkTheme: Boolean = LocalUseDarkTheme.current,
    useAmoledBlackTheme: Boolean = false,
    useDynamicTheme: Boolean = false,
    setSystemBarStyles: (SystemBarStyle, SystemBarStyle) -> Unit = { _, _ -> },
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

    val context = LocalContext.current

    MaterialTheme(
        colorScheme = when {
            useDynamicTheme && useDarkTheme -> dynamicDarkColorScheme(context)
            useDynamicTheme && !useDarkTheme -> dynamicLightColorScheme(context)
            !useDynamicTheme && useDarkTheme -> darkColors
            else -> lightColors
        }
            .run {
                if (useAmoledBlackTheme && useDarkTheme) {
                    copy(background = Color.Black, surface = Color.Black)
                } else {
                    this
                }
            }
            .animate(animationSpec = remember { spring(stiffness = Spring.StiffnessMedium) }),
        typography = typography
    ) {
        content()
    }
}

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
    animateColorAsState(this, animationSpec, label = "").value
