package com.w2sv.wifiwidget.ui.designsystem.theme

import androidx.compose.runtime.Stable
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.ThemeSettings

@Stable
data class ThemeController(val settings: ThemeSettings, val updateSettings: ((ThemeSettings) -> ThemeSettings) -> Unit) {
    companion object {
        val Default = ThemeController(
            settings = ThemeSettings(Theme.Default, useAmoledBlackTheme = true, useDynamicColors = true),
            updateSettings = {}
        )
    }
}
