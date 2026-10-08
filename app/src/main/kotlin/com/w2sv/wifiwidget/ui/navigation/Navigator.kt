package com.w2sv.wifiwidget.ui.navigation

import androidx.navigation3.runtime.NavBackStack

class Navigator(backStack: NavBackStack<Screen>) : Nav3Navigator<Screen>(backStack) {

    fun toWidgetConfiguration() =
        launchSingleTop(Screen.WidgetConfiguration)
}
