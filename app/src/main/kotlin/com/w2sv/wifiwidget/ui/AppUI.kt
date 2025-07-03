package com.w2sv.wifiwidget.ui

import android.location.LocationManager
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.domain.model.Theme
import com.w2sv.wifiwidget.ui.navigation.NavGraph
import com.w2sv.wifiwidget.ui.navigation.Screen
import com.w2sv.wifiwidget.ui.sharedviewmodel.AppViewModel
import com.w2sv.wifiwidget.ui.states.rememberLocationAccessState
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.utils.activityViewModel

@Composable
fun AppUI(
    initialScreen: Screen,
    locationManager: LocationManager,
    setSystemBarStyles: (SystemBarStyle, SystemBarStyle) -> Unit,
    appVM: AppViewModel = activityViewModel()
) {
    val theme by appVM.theme.collectAsStateWithLifecycle()
    val useAmoledBlackTheme by appVM.useAmoledBlackTheme.collectAsStateWithLifecycle()
    val useDynamicColors by appVM.useDynamicColors.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalUseDarkTheme provides rememberUseDarkTheme(theme = theme),
        LocalLocationAccessState provides rememberLocationAccessState(),
        LocalLocationManager provides locationManager
    ) {
        AppTheme(
            useDynamicTheme = useDynamicColors,
            useAmoledBlackTheme = useAmoledBlackTheme,
            setSystemBarStyles = setSystemBarStyles
        ) {
            NavGraph(initialScreen = initialScreen)
        }
    }
}

@Composable
private fun rememberUseDarkTheme(theme: Theme): Boolean {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    return remember(theme, isSystemInDarkTheme) {
        when (theme) {
            Theme.Light -> false
            Theme.Dark -> true
            Theme.Default -> isSystemInDarkTheme
        }
    }
}
