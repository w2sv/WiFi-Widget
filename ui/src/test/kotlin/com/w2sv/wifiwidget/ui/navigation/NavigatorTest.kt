package com.w2sv.wifiwidget.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigatorTest {

    @Test
    fun `widget configuration is launched only once when already on top`() {
        val backStack = NavBackStack<Screen>(Screen.Home)
        val navigator = Navigator(backStack)

        navigator.toWidgetConfiguration()
        assertEquals(listOf(Screen.Home, Screen.WidgetConfiguration), backStack.toList())

        navigator.toWidgetConfiguration()
        assertEquals(listOf(Screen.Home, Screen.WidgetConfiguration), backStack.toList())
    }
}
