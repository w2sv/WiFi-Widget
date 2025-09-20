package com.w2sv.wifiwidget.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import com.w2sv.wifiwidget.ui.noCompositionLocalProvidedFor

interface Navigator {
    fun toWidgetConfiguration()
    fun leaveWidgetConfiguration()
    fun popBackStack()
}

class NavigatorImpl(backStack: NavBackStack<Screen>) :
    Nav3Navigator<Screen>(backStack),
    Navigator {

    override fun toWidgetConfiguration() =
        launchSingleTop(Screen.WidgetConfiguration)

    override fun leaveWidgetConfiguration() {
        if (backStack.size == 1) {
            clearAndLaunch(Screen.Home)
        } else {
            popBackStack()
        }
    }
}

class PreviewNavigator : Navigator {
    override fun toWidgetConfiguration() {}
    override fun leaveWidgetConfiguration() {}
    override fun popBackStack() {}
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    noCompositionLocalProvidedFor("LocalNavigator")
}
