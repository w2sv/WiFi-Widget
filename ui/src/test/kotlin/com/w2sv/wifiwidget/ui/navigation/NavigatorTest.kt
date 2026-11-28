package com.w2sv.wifiwidget.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessScreenScenario
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessScreenScenario.ManageAccess
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class NavigatorTest {

    @Test
    fun `location access launches Manage Access only once when already on top`() {
        val backStack = NavBackStack<Screen>(Screen.Home)
        val navigator = Navigator(backStack)

        navigator.toLocationAccess()
        assertEquals(listOf(Screen.Home, Screen.LocationAccess(ManageAccess)), backStack.toList())

        navigator.toLocationAccess()
        assertEquals(listOf(Screen.Home, Screen.LocationAccess(ManageAccess)), backStack.toList())
    }

    @Test
    fun `completing onboarding clears the back stack and launches Home`() {
        val backStack = NavBackStack<Screen>(Screen.LocationAccessOnboarding)

        Navigator(backStack).launchHome()

        assertEquals(listOf(Screen.Home), backStack.toList())
    }

    @Test
    fun `widget configuration is launched only once when already on top`() {
        val backStack = NavBackStack<Screen>(Screen.Home)
        val navigator = Navigator(backStack)

        navigator.toWidgetConfiguration()
        assertEquals(listOf(Screen.Home, Screen.WidgetConfiguration), backStack.toList())

        navigator.toWidgetConfiguration()
        assertEquals(listOf(Screen.Home, Screen.WidgetConfiguration), backStack.toList())
    }

    @Test
    fun `location access destinations retain both scenarios through serialization`() {
        val serializer = NavBackStackSerializer<Screen>()

        for (scenario in LocationAccessScreenScenario.entries) {
            val backStack = when (scenario) {
                Onboarding -> NavBackStack<Screen>(Screen.LocationAccessOnboarding)
                ManageAccess -> NavBackStack<Screen>(Screen.Home, Screen.LocationAccess(scenario))
            }
            val restored = decodeFromSavedState(serializer, encodeToSavedState(serializer, backStack))

            assertEquals(backStack.toList(), restored.toList())
        }
    }
}
