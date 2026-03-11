package com.w2sv.wifiwidget.ui

import androidx.activity.SystemBarStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.wifiwidget.ui.navigation.NavGraph
import com.w2sv.wifiwidget.ui.navigation.Screen
import com.w2sv.wifiwidget.ui.sharedstate.location.OptionalLocationAccessRationals
import com.w2sv.wifiwidget.ui.sharedstate.location.access_capability.rememberLocationAccessCapability
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.util.activityViewModel
import com.w2sv.wifiwidget.ui.util.useDarkTheme

@Composable
fun AppUI(
    initialScreen: Screen,
    isGpsEnabled: () -> Boolean,
    setSystemBarStyles: (SystemBarStyle, SystemBarStyle) -> Unit,
    appVM: AppViewModel = activityViewModel()
) {
    val theme by appVM.theme.collectAsStateWithLifecycle()
    val useAmoledBlackTheme by appVM.useAmoledBlackTheme.collectAsStateWithLifecycle()
    val useDynamicColors by appVM.useDynamicColors.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalLocationAccessCapability provides rememberLocationAccessCapability(isGpsEnabled = isGpsEnabled)) {
        AppTheme(
            useDarkTheme = useDarkTheme(theme),
            useDynamicColors = useDynamicColors,
            useAmoledBlackTheme = useAmoledBlackTheme,
            setSystemBarStyles = setSystemBarStyles
        ) {
            NavGraph(initialScreen = initialScreen)
            OptionalLocationAccessRationals()
        }
    }
}
