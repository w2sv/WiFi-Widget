package com.w2sv.wifiwidget.storescreenshots

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.WorkManager
import com.w2sv.domain.location.LocationAccessRepository
import com.w2sv.domain.preferences.PreferencesRepository
import com.w2sv.domain.preferences.Theme
import com.w2sv.domain.widget.WidgetConfigDataSource
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.presentation.resolve
import com.w2sv.wifiwidget.storescreenshots.device.ScreenshotDevice
import com.w2sv.wifiwidget.storescreenshots.device.ScreenshotDeviceSetup
import com.w2sv.wifiwidget.storescreenshots.device.ScreenshotSystemUi
import com.w2sv.wifiwidget.storescreenshots.device.ScreenshotWallpaper
import com.w2sv.wifiwidget.storescreenshots.scenario.ActiveScreenshotScenario
import com.w2sv.wifiwidget.storescreenshots.scenario.ScreenshotScenario
import com.w2sv.wifiwidget.storescreenshots.scenario.ScreenshotScenarios
import com.w2sv.wifiwidget.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private enum class InAppScreenshot(val filename: String, val scenario: ScreenshotScenario, val theme: Theme) {
    Home("02-app-home.png", ScreenshotScenarios.networkOverview, Theme.Light),
    Appearance("04-app-appearance.png", ScreenshotScenarios.customizedWidget, Theme.Dark),
    Properties("06-app-properties.png", ScreenshotScenarios.transparentWidget, Theme.Light)
}

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class InAppScreenshotTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var widgetConfigDataSource: WidgetConfigDataSource

    @Inject
    lateinit var widgetConfigSource: WidgetConfigSource

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    lateinit var locationAccessRepository: LocationAccessRepository

    @Inject
    lateinit var activeScenario: ActiveScreenshotScenario

    private val screenshotDevice = ScreenshotDevice()
    private val wallpaper = ScreenshotWallpaper(screenshotDevice)
    private val context = screenshotDevice.targetContext

    @Test
    fun `capture paired in-app screenshots`() {
        hiltRule.inject()
        initializeWorkManager()
        ScreenshotDeviceSetup(screenshotDevice).prepare()
        prepareAppState()

        InAppScreenshot.entries.forEach(::capture)
    }

    private fun initializeWorkManager() {
        if (runCatching { WorkManager.getInstance(context) }.isFailure) {
            WorkManager.initialize(context, Configuration.Builder().setWorkerFactory(workerFactory).build())
        }
    }

    private fun prepareAppState() {
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ).forEach { permission ->
            screenshotDevice.executeShellCommand("pm grant ${screenshotDevice.appPackage} $permission")
        }
        runBlocking {
            locationAccessRepository.recordRationalShown()
            locationAccessRepository.rationalShown.first { it }
        }
    }

    private fun capture(screenshot: InAppScreenshot) {
        println("Screenshot Gen: preparing ${screenshot.filename}")
        val scenario = screenshot.scenario
        activeScenario.select(scenario)
        wallpaper.set(scenario.wallpaperRes)
        val expectedConfig = scenario.widgetConfig
        runBlocking {
            preferencesRepository.updateThemeSettings {
                it.copy(
                    theme = screenshot.theme,
                    useDynamicColors = true,
                    useAmoledBlackTheme = false
                )
            }
            preferencesRepository.themeSettings.first {
                it.theme == screenshot.theme &&
                    it.useDynamicColors &&
                    !it.useAmoledBlackTheme
            }
            widgetConfigDataSource.update { expectedConfig }
            widgetConfigSource.flow.first { it == expectedConfig }
            check(widgetConfigSource.snapshot() == expectedConfig) {
                "The configuration for ${screenshot.filename} was not persisted."
            }
        }

        ActivityScenario.launch(MainActivity::class.java).use {
            when (screenshot) {
                InAppScreenshot.Home -> showHome(scenario)
                InAppScreenshot.Appearance -> showAppearance()
                InAppScreenshot.Properties -> showProperties(scenario)
            }
            composeRule.waitForIdle()
            val systemUi = ScreenshotSystemUi(screenshotDevice)
            try {
                systemUi.enterDemoMode()
                composeRule.waitForIdle()
                screenshotDevice.captureScreenshot(screenshot.filename)
            } finally {
                systemUi.exitDemoMode()
            }
        }
        println("Screenshot Gen: captured ${screenshot.filename}")
    }

    private fun showHome(scenario: ScreenshotScenario) {
        val firstPropertyValue = scenario.properties.first().values.first().value.resolve(context.resources)
        composeRule.onNodeWithText(firstPropertyValue).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(com.w2sv.core.common.R.string.configure)).assertIsDisplayed()
    }

    private fun openConfiguration() {
        composeRule.onNodeWithText(context.getString(com.w2sv.core.common.R.string.configure)).performClick()
        composeRule.onNodeWithTag("widget_config_list").assertIsDisplayed()
    }

    private fun showAppearance() {
        openConfiguration()
        composeRule.onNodeWithTag("widget_config_list").performScrollToIndex(0)
        composeRule.onNodeWithTag("appearance_section").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(com.w2sv.core.common.R.string.custom)).assertIsSelected()
    }

    private fun showProperties(scenario: ScreenshotScenario) {
        openConfiguration()
        composeRule.onNodeWithTag("widget_config_list").performScrollToIndex(1)
        val title = composeRule.onNodeWithText(context.getString(com.w2sv.core.common.R.string.widget_configuration))
        val expandedTitleTop = title.fetchSemanticsNode().boundsInRoot.top
        composeRule.onNodeWithTag("widget_config_list").performTouchInput {
            swipeUp(startY = height * 0.70f, endY = height * 0.64f)
        }
        composeRule.waitForIdle()
        check(title.fetchSemanticsNode().boundsInRoot.top < expandedTitleTop) {
            "The Widget Configuration top bar did not collapse before capturing Properties."
        }
        composeRule.onNodeWithTag("wifi_properties_section").assertIsDisplayed()
        val firstProperty = scenario.widgetConfig.enabledProperties.first()
        val firstIpProperty = scenario.widgetConfig.enabledProperties.filterIsInstance<WifiProperty.IpProperty>().first()
        composeRule.onNodeWithTag("wifi_property_${firstProperty.labelRes}").assertIsDisplayed()
        composeRule.onNodeWithTag("expand_ip_settings_${firstIpProperty.labelRes}").performClick()
        composeRule.onNodeWithText(context.getString(firstIpProperty.settings.first().labelRes)).assertIsDisplayed()
    }
}
