package com.w2sv.wifiwidget.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.w2sv.wifiwidget.R

@Composable
fun WifiWidgetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = colorResource(id = R.color.primary),
            onPrimary = Color.White,
            surface = Color.White,
            background = Color.White,
            secondary = colorResource(id = R.color.mischka_dark),
            tertiary = colorResource(id = R.color.mischka)
        )
    ) {
        content()
    }
}