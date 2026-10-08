package com.w2sv.wifiwidget.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.common.utils.IsLocationEnabled
import com.w2sv.wifiwidget.ui.designsystem.theme.ThemeController
import com.w2sv.wifiwidget.ui.designsystem.theme.WifiWidgetTheme
import com.w2sv.wifiwidget.ui.location.OptionalLocationAccessRationals
import com.w2sv.wifiwidget.ui.location.capability.access.LocalLocationAccessCapability
import com.w2sv.wifiwidget.ui.location.capability.access.rememberLocationAccessCapability
import com.w2sv.wifiwidget.ui.navigation.NavGraph
import com.w2sv.wifiwidget.ui.util.useDarkTheme

@Composable
fun WifiWidgetUI(
    isLocationEnabled: IsLocationEnabled,
    activityViewModel: ActivityViewModel = hiltViewModel(),
    // Lets both activities share app-level UI and tests omit destination Hilt ViewModels.
    content: @Composable (ThemeController) -> Unit = ::NavGraph
) {
    val themeSettings by activityViewModel.themeSettings.collectAsStateWithLifecycle()
    val themeController =
        remember(themeSettings, activityViewModel) { ThemeController(themeSettings, activityViewModel::updateThemeSettings) }

    val locationAccessCapability = rememberLocationAccessCapability(
        isLocationEnabled = isLocationEnabled,
        requestHistory = activityViewModel.locationAccessPermissionHistory,
        rationalHistory = activityViewModel.locationAccessRationalHistory
    )

    CompositionLocalProvider(LocalLocationAccessCapability provides locationAccessCapability) {
        WifiWidgetTheme(
            useDarkTheme = useDarkTheme(themeSettings.theme),
            useDynamicColors = themeSettings.useDynamicColors,
            useAmoledBlackTheme = themeSettings.useAmoledBlackTheme
        ) {
            content(themeController)
            OptionalLocationAccessRationals()
        }
    }
}
