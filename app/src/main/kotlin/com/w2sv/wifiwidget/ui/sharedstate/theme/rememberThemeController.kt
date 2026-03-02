package com.w2sv.wifiwidget.ui.sharedstate.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.wifiwidget.ui.AppViewModel
import com.w2sv.wifiwidget.ui.util.activityViewModel

@Composable
fun rememberThemeController(appVM: AppViewModel = activityViewModel()): ThemeController {
    val theme by appVM.theme.collectAsStateWithLifecycle()
    val useAmoledBlackTheme by appVM.useAmoledBlackTheme.collectAsStateWithLifecycle()
    val useDynamicColors by appVM.useDynamicColors.collectAsStateWithLifecycle()

    return remember(appVM) {
        ThemeController(
            theme = { theme },
            setTheme = appVM::saveTheme,
            useAmoledBlackTheme = { useAmoledBlackTheme },
            setUseAmoledBlackTheme = appVM::saveUseAmoledBlackTheme,
            useDynamicColors = { useDynamicColors },
            setUseDynamicColors = appVM::saveUseDynamicColors
        )
    }
}
