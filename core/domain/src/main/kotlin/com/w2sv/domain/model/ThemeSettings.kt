package com.w2sv.domain.model

import com.w2sv.androidutils.os.dynamicColorsSupported

data class ThemeSettings(val theme: Theme, val useAmoledBlackTheme: Boolean, val useDynamicColors: Boolean) {
    companion object {
        val Default = ThemeSettings(Theme.Default, false, dynamicColorsSupported)
    }
}
