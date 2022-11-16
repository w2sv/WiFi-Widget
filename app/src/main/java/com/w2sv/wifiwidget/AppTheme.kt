package com.w2sv.wifiwidget

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = colorResource(id = R.color.blue_chill_dark),
            onPrimary = Color.White,
            secondary = Color.Magenta,
            surface = Color.White,
            background = Color.White
        )
    ) {
        content()
    }
}