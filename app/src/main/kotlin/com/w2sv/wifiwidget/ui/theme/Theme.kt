package com.w2sv.wifiwidget.ui.theme

import android.annotation.SuppressLint
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.w2sv.wifiwidget.R

@SuppressLint("NewApi")
@Composable
fun AppTheme(
    useDynamicTheme: Boolean = false,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = when {
            useDynamicTheme && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
            useDynamicTheme && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
            !useDynamicTheme && darkTheme -> darkColorScheme(
                primary = colorResource(id = R.color.dark_cyan),
                onPrimary = Color.White,

                secondary = colorResource(id = R.color.magenta_haze_light),

                onSurface = Color.White,
                onSurfaceVariant = colorResource(id = R.color.light_gray),

                onBackground = Color.White
            )

            else -> lightColorScheme(
                primary = colorResource(id = R.color.dark_cyan),
                onPrimary = Color.White,

                secondary = colorResource(id = R.color.magenta_haze_dark),

                onSurface = Color.Black,
                onSurfaceVariant = colorResource(id = R.color.dark_gray),

                onBackground = Color.Black
            )
        }
    ) {
        content()
    }
}