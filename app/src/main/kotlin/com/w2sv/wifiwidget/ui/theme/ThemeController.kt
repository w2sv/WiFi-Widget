package com.w2sv.wifiwidget.ui.theme

import androidx.compose.runtime.Stable
import com.w2sv.domain.model.Theme

@Stable
data class ThemeController(
    val theme: () -> Theme,
    val setTheme: (Theme) -> Unit,
    val useAmoledBlackTheme: () -> Boolean,
    val setUseAmoledBlackTheme: (Boolean) -> Unit,
    val useDynamicColors: () -> Boolean,
    val setUseDynamicColors: (Boolean) -> Unit
)

fun previewThemeController() =
    ThemeController(
        theme = { Default },
        setTheme = {},
        useAmoledBlackTheme = { true },
        setUseAmoledBlackTheme = {},
        useDynamicColors = { true },
        setUseDynamicColors = {}
    )
