package com.w2sv.wifiwidget.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.theme.ThemeController
import com.w2sv.wifiwidget.ui.designsystem.theme.WifiWidgetTheme
import com.w2sv.wifiwidget.ui.location.LocationAccessDependencies
import com.w2sv.wifiwidget.ui.location.capability.access.LocalLocationAccessCapability
import com.w2sv.wifiwidget.ui.location.capability.access.rememberLocationAccessCapability
import com.w2sv.wifiwidget.ui.navigation.NavGraph
import com.w2sv.wifiwidget.ui.util.snackbar.LocalSnackbarVisibility
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarVisibility
import com.w2sv.wifiwidget.ui.util.useDarkTheme

@Composable
fun WifiWidgetUI(
    versionName: String,
    locationAccessDependencies: LocationAccessDependencies,
    activityViewModel: ActivityViewModel = hiltViewModel(),
    // Lets both activities share app-level UI and tests omit destination Hilt ViewModels.
    content: @Composable (ThemeController) -> Unit = { NavGraph(it) }
) {
    val themeSettings by activityViewModel.themeSettings.collectAsStateWithLifecycle()
    val themeController = remember(themeSettings, activityViewModel) {
        ThemeController(themeSettings, activityViewModel::updateThemeSettings)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarVisibility = remember { SnackbarVisibility() }

    val locationAccessCapability = rememberLocationAccessCapability(
        isLocationEnabled = locationAccessDependencies.isLocationEnabled,
        requestHistory = locationAccessDependencies.requestHistory
    )

    CompositionLocalProvider(
        LocalAppVersionName provides versionName,
        LocalLocationAccessCapability provides locationAccessCapability,
        LocalSnackbarHostState provides snackbarHostState,
        LocalSnackbarVisibility provides snackbarVisibility
    ) {
        WifiWidgetTheme(
            useDarkTheme = useDarkTheme(themeSettings.theme),
            useDynamicColors = themeSettings.useDynamicColors,
            useAmoledBlackTheme = themeSettings.useAmoledBlackTheme
        ) {
            content(themeController)
        }
    }
}
