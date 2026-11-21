package com.w2sv.wifiwidget.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.hilt.lifecycle.viewmodel.HiltViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavBackStack
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.wifiwidget.ui.designsystem.theme.ThemeController
import com.w2sv.wifiwidget.ui.location.LocationPermissionLevel
import com.w2sv.wifiwidget.ui.location.capability.access.LocalLocationAccessCapability
import com.w2sv.wifiwidget.ui.location.capability.access.LocationAccessCapability
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessScreenScenario.EnableWidgetProperty
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessScreenScenario.ManagePermissions
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessScreenViewModel
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkConstructor
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class NavGraphTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @After
    fun tearDown() {
        unmockkConstructor(Navigator::class)
        unmockkStatic(::HiltViewModelFactory)
    }

    @Test
    fun `NavDisplay system Back invokes navigator onBack`() {
        val backStack = NavBackStack<Screen>(
            Screen.LocationAccess(ManagePermissions),
            Screen.LocationAccess(EnableWidgetProperty(WifiProperty.SSID.protoId))
        )
        mockkConstructor(Navigator::class)
        every { anyConstructed<Navigator>().backStack } returns backStack
        every { anyConstructed<Navigator>().onBack() } just Runs

        val factory = viewModelFactory {
            initializer {
                LocationAccessScreenViewModel(
                    configDataSource = mockk(relaxed = true),
                    locationAccessRepository = mockk(relaxed = true),
                    savedStateHandle = SavedStateHandle(),
                    scenario = EnableWidgetProperty(WifiProperty.SSID.protoId)
                )
            }
        }
        mockkStatic(::HiltViewModelFactory)
        every { HiltViewModelFactory(any(), any()) } returns factory
        val capability = mockk<LocationAccessCapability>(relaxed = true) {
            every { permissionLevel } returns LocationPermissionLevel.PreciseBackground
            every { locationServicesEnabled } returns true
            every { locationServicesEnabledFlow } returns flowOf(true)
            every { foregroundRequestResults } returns emptyFlow()
            every { backgroundRequestResults } returns emptyFlow()
        }
        composeRule.setContent {
            CompositionLocalProvider(
                LocalLocationAccessCapability provides capability,
                LocalInspectionMode provides true
            ) {
                NavGraph(themeController = ThemeController.Default)
            }
        }
        composeRule.waitForIdle()

        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }

        composeRule.runOnIdle {
            verify(exactly = 1) { anyConstructed<Navigator>().onBack() }
            verify(exactly = 0) { anyConstructed<Navigator>().popBackStack() }
            verify(exactly = 0) { anyConstructed<Navigator>().popLocationAccess() }
        }
    }
}
