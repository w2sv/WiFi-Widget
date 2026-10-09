package com.w2sv.wifiwidget.ui.designsystem.theme

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.composed.ui.systembar.SystemBarIconAppearance

@Composable
fun WifiWidgetTheme(
    useDarkTheme: Boolean = false,
    useAmoledBlackTheme: Boolean = false,
    useDynamicColors: Boolean = false,
    context: Context = LocalContext.current,
    content: @Composable () -> Unit
) {
    SystemBarIconAppearance(useDarkTheme)

    val enableDynamicColors = useDynamicColors && dynamicColorsSupported
    val colorScheme = when {
        enableDynamicColors && useDarkTheme && useAmoledBlackTheme -> dynamicDarkColorScheme(context).amoledBlack()
        enableDynamicColors && useDarkTheme -> dynamicDarkColorScheme(context)
        enableDynamicColors -> dynamicLightColorScheme(context)
        useDarkTheme && useAmoledBlackTheme -> darkColors.amoledBlack()
        useDarkTheme -> darkColors
        else -> lightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WiFiWidgetTypography,
        content = content
    )
}

private fun ColorScheme.amoledBlack(): ColorScheme =
    copy(background = Black, surface = Black, onBackground = White, onSurface = White)
