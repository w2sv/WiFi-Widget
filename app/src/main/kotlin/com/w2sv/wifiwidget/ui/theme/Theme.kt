package com.w2sv.wifiwidget.ui.theme

import android.annotation.SuppressLint
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@SuppressLint("NewApi")
@Composable
fun AppTheme(
    useDynamicTheme: Boolean = false,
    useDarkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    MaterialTheme(
        colorScheme = when {
            useDynamicTheme && useDarkTheme -> dynamicDarkColorScheme(context)
            useDynamicTheme && !useDarkTheme -> dynamicLightColorScheme(context)
            !useDynamicTheme && useDarkTheme -> darkColors
            else -> lightColors
        },
        typography = typography
    ) {
        content()
    }
}
