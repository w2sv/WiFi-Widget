package com.w2sv.wifiwidget.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import com.w2sv.composed.navigation3.Nav3Navigator
import com.w2sv.composed.navigation3.rememberTypedNavBackStack
import com.w2sv.composed.runtime.OnChange
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessScreenScenario
import slimber.log.i

class Navigator(backStack: NavBackStack<Screen>) : Nav3Navigator<Screen>(backStack) {

    fun toLocationAccess() =
        launchSingleTop(Screen.LocationAccess(LocationAccessScreenScenario.ManageAccess))

    fun launchHome() {
        clearAndLaunch(Screen.Home)
    }

    fun toWidgetConfiguration() =
        launchSingleTop(Screen.WidgetConfiguration)
}

@Composable
fun rememberNavigator(initialScreen: Screen = Screen.Home): Navigator {
    val backStack = rememberTypedNavBackStack<Screen>(initialScreen)
    val navigator = remember(backStack) { Navigator(backStack) }

    OnChange(backStack.size) { i { "BackStack=${backStack.map { screen -> screen::class.java.simpleName }}" } }

    return navigator
}
