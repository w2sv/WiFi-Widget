package com.w2sv.wifiwidget.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.w2sv.wifiwidget.R

@Composable
fun WifiWidgetTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme)
            darkColorScheme(
                primary = colorResource(id = R.color.primary),
                inversePrimary = colorResource(id = com.w2sv.common.R.color.magenta_haze),
                onPrimary = Color.Black,
                surface = Color.Black,
                background = Color.Black,
                secondary = colorResource(id = com.w2sv.common.R.color.cadet_gray),
                tertiary = colorResource(id = com.w2sv.common.R.color.ghost_white)
            ) else
            lightColorScheme(
                primary = colorResource(id = R.color.primary),
                inversePrimary = colorResource(id = com.w2sv.common.R.color.magenta_haze),
                onPrimary = Color.White,
                surface = Color.White,
                background = Color.White,
                secondary = colorResource(id = com.w2sv.common.R.color.cadet_gray),
                tertiary = colorResource(id = com.w2sv.common.R.color.ghost_white)
            )
    ) {
        content()
    }
}