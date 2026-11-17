package com.w2sv.wifiwidget.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessScreenScenario.EnableWidgetProperty
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class NavigatorTest {

    private val propertyId = WifiProperty.entries.first().protoId

    @Test
    fun `ordinary pops including Location Access do not send results`() {
        val resultEventBus = mockk<ResultEventBus>(relaxed = true)
        val backStack = NavBackStack(
            Screen.Home,
            Screen.WidgetConfiguration,
            Screen.LocationAccess(EnableWidgetProperty(propertyId))
        )
        val navigator = Navigator(backStack, resultEventBus)

        navigator.popBackStack()
        assertEquals(listOf(Screen.Home, Screen.WidgetConfiguration), backStack.toList())
        navigator.popBackStack()
        assertEquals(listOf(Screen.Home), backStack.toList())

        verify(exactly = 0) { resultEventBus.sendResult(any<String>(), any<Any>()) }
    }

    @Test
    fun `Back from widget configuration pops without sending a result`() {
        val resultEventBus = mockk<ResultEventBus>(relaxed = true)
        val backStack = NavBackStack(Screen.Home, Screen.WidgetConfiguration)
        val navigator = Navigator(backStack, resultEventBus)

        navigator.onBack()

        verify(exactly = 0) { resultEventBus.sendResult(any<String>(), any<Any>()) }
        assertEquals(listOf(Screen.Home), backStack.toList())
    }

    @Test
    fun `Back from Location Access sends the result once before popping`() {
        val resultEventBus = mockk<ResultEventBus>(relaxed = true)
        val result = EnableWidgetProperty(propertyId)
        val screen = Screen.LocationAccess(result)
        val backStack = NavBackStack(Screen.WidgetConfiguration, screen)
        val navigator = Navigator(backStack, resultEventBus)
        every { resultEventBus.sendResult(EnableWidgetProperty::class.toString(), result) } answers {
            assertEquals(screen, navigator.currentRoute)
        }

        navigator.onBack()
        assertEquals(listOf(Screen.WidgetConfiguration), backStack.toList())
        navigator.popLocationAccess()

        verify(exactly = 1) { resultEventBus.sendResult(EnableWidgetProperty::class.toString(), result) }
        assertEquals(listOf(Screen.WidgetConfiguration), backStack.toList())
    }

    @Test
    fun `Location Access at the root does not send a result or pop`() {
        val resultEventBus = mockk<ResultEventBus>(relaxed = true)
        val screen = Screen.LocationAccess(EnableWidgetProperty(propertyId))
        val backStack = NavBackStack<Screen>(screen)

        Navigator(backStack, resultEventBus).onBack()

        verify(exactly = 0) { resultEventBus.sendResult(any<String>(), any<Any>()) }
        assertEquals(listOf(screen), backStack.toList())
    }

    @Test
    fun `widget configuration is launched only once when already on top`() {
        val backStack = NavBackStack<Screen>(Screen.Home)
        val navigator = Navigator(backStack, ResultEventBus())

        navigator.toWidgetConfiguration()
        assertEquals(listOf(Screen.Home, Screen.WidgetConfiguration), backStack.toList())

        navigator.toWidgetConfiguration()
        assertEquals(listOf(Screen.Home, Screen.WidgetConfiguration), backStack.toList())
    }

    @Test
    fun `location access destination retains the requested property through serialization`() {
        val backStack = NavBackStack(
            Screen.WidgetConfiguration,
            Screen.LocationAccess(EnableWidgetProperty(propertyId))
        )
        val serializer = NavBackStackSerializer<Screen>()

        val restored = decodeFromSavedState(serializer, encodeToSavedState(serializer, backStack))

        assertEquals(backStack.toList(), restored.toList())
    }
}
