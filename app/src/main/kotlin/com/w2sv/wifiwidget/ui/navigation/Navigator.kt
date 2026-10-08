package com.w2sv.wifiwidget.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.w2sv.composed.runtime.OnChange
import slimber.log.i

class Navigator(backStack: NavBackStack<Screen>) : Nav3Navigator<Screen>(backStack) {

    fun toWidgetConfiguration() =
        launchSingleTop(Screen.WidgetConfiguration)
}

@Composable
fun rememberNavigator(): Navigator {
    val backStack = rememberTypedNavBackStack<Screen>(Screen.Home)
    val navigator = remember(backStack) { Navigator(backStack) }

    OnChange(backStack.size) { i { "BackStack=${backStack.map { screen -> screen::class.java.simpleName }}" } }

    return navigator
}

@Composable
@Suppress("UNCHECKED_CAST")
private fun <T : NavKey> rememberTypedNavBackStack(vararg initialKeys: T): NavBackStack<T> =
    rememberNavBackStack(*initialKeys) as NavBackStack<T>
