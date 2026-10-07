package com.w2sv.wifiwidget.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.wifiwidget.ui.designsystem.theme.ThemeController
import com.w2sv.wifiwidget.ui.designsystem.theme.WifiWidgetTheme
import com.w2sv.wifiwidget.ui.location.OptionalLocationAccessRationals
import com.w2sv.wifiwidget.ui.location.capability.access.rememberLocationAccessCapability
import com.w2sv.wifiwidget.ui.navigation.NavGraph
import com.w2sv.wifiwidget.ui.navigation.Screen
import com.w2sv.wifiwidget.ui.util.useDarkTheme

@Composable
fun WifiWidgetUI(
    initialScreen: Screen,
    isGpsEnabled: () -> Boolean,
    activityVM: MainActivityViewModel = hiltViewModel()
) {
    val themeSettings by activityVM.themeSettings.collectAsStateWithLifecycle()
    val themeController = remember(themeSettings, activityVM) { ThemeController(themeSettings, activityVM::updateThemeSettings) }

    val locationAccessCapability = rememberLocationAccessCapability(
        isGpsEnabled = isGpsEnabled,
        requestHistory = activityVM.locationAccessPermissionHistory,
        rationalHistory = activityVM.locationAccessRationalHistory
    )

    CompositionLocalProvider(LocalLocationAccessCapability provides locationAccessCapability) {
        WifiWidgetTheme(
            useDarkTheme = useDarkTheme(themeSettings.theme),
            useDynamicColors = themeSettings.useDynamicColors,
            useAmoledBlackTheme = themeSettings.useAmoledBlackTheme
        ) {
            NavGraph(initialScreen = initialScreen, themeController = themeController)
            OptionalLocationAccessRationals()
        }
    }
}
