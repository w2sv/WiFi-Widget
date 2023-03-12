package com.w2sv.wifiwidget.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.w2sv.wifiwidget.R

@Composable
fun WifiWidgetTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme)
            darkColorScheme(
                primary = colorResource(id = R.color.dark_cyan),
                onPrimary = Color.White,

                secondary = colorResource(id = R.color.magenta_haze),

                onSurface = Color.White,
                onSurfaceVariant = colorResource(id = R.color.cadet_gray),

                onBackground = Color.White
            ) else
            lightColorScheme(
                primary = colorResource(id = R.color.dark_cyan),
                onPrimary = Color.White,

                secondary = colorResource(id = R.color.magenta_haze),

                onSurface = Color.Black,
                onSurfaceVariant = colorResource(id = R.color.cadet_gray),

                onBackground = Color.Black
            )
    ) {
        content()
    }
}