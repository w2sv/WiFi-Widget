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
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    MaterialTheme(
        colorScheme = when {
            useDynamicTheme && darkTheme -> dynamicDarkColorScheme(context)
            useDynamicTheme && !darkTheme -> dynamicLightColorScheme(context)
            !useDynamicTheme && darkTheme -> darkColors
            else -> lightColors
        },
        typography = typography
    ) {
        content()
    }
}
