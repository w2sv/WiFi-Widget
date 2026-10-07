package com.w2sv.wifiwidget.ui.designsystem.theme

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat

@Composable
internal fun SystemBarAppearance(useDarkTheme: Boolean) {
    val activity = LocalActivity.current ?: return

    SideEffect {
        val controller =
            WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        controller.isAppearanceLightStatusBars = !useDarkTheme
        controller.isAppearanceLightNavigationBars = !useDarkTheme
    }
}
