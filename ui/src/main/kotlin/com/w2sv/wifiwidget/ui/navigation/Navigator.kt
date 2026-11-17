package com.w2sv.wifiwidget.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.runtime.result.rememberResultEventBus
import com.w2sv.composed.navigation3.Nav3Navigator
import com.w2sv.composed.navigation3.rememberTypedNavBackStack
import com.w2sv.composed.runtime.OnChange
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessScreenScenario
import slimber.log.i

class Navigator(backStack: NavBackStack<Screen>, val resultEventBus: ResultEventBus) : Nav3Navigator<Screen>(backStack) {

    fun onBack() {
        if (currentRoute is Screen.LocationAccess) {
            popLocationAccess()
        } else {
            popBackStack()
        }
    }

    fun popLocationAccess() {
        val screen = currentRoute as? Screen.LocationAccess ?: return
        if (backStack.size <= 1) return

        val scenario = screen.scenario
        if (scenario is LocationAccessScreenScenario.EnableWidgetProperty) {
            resultEventBus.sendResult(scenario)
        }
        popBackStack()
    }

    fun toLocationAccess(scenario: LocationAccessScreenScenario) {
        launchSingleTop(Screen.LocationAccess(scenario))
    }

    fun launchHome() {
        clearAndLaunch(Screen.Home)
    }

    fun toWidgetConfiguration() =
        launchSingleTop(Screen.WidgetConfiguration)
}

@Composable
fun rememberNavigator(initialScreen: Screen = Screen.Home): Navigator {
    val backStack = rememberTypedNavBackStack<Screen>(initialScreen)
    val resultEventBus = rememberResultEventBus()
    val navigator = remember(backStack, resultEventBus) { Navigator(backStack, resultEventBus) }

    OnChange(backStack.size) { i { "BackStack=${backStack.map { screen -> screen::class.java.simpleName }}" } }

    return navigator
}
