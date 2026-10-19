package com.w2sv.wifiwidget.storescreenshots

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.work.Configuration
import androidx.work.WorkManager
import com.w2sv.domain.widget.WidgetConfigDataSource
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.domain.widget.WidgetUtility
import com.w2sv.wifiwidget.storescreenshots.device.LAUNCHER_PACKAGE
import com.w2sv.wifiwidget.storescreenshots.device.ScreenshotDevice
import com.w2sv.wifiwidget.storescreenshots.device.ScreenshotDeviceSetup
import com.w2sv.wifiwidget.storescreenshots.device.ScreenshotSystemUi
import com.w2sv.wifiwidget.storescreenshots.device.ScreenshotWallpaper
import com.w2sv.wifiwidget.storescreenshots.launcher.LauncherWidgetController
import com.w2sv.wifiwidget.storescreenshots.launcher.LauncherWorkspace
import com.w2sv.wifiwidget.storescreenshots.scenario.ActiveScreenshotScenario
import com.w2sv.wifiwidget.storescreenshots.scenario.ScreenshotScenario
import com.w2sv.wifiwidget.storescreenshots.scenario.ScreenshotScenarios
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class WidgetLauncherScreenshotTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var widgetConfigDataSource: WidgetConfigDataSource

    @Inject
    lateinit var widgetConfigSource: WidgetConfigSource

    @Inject
    lateinit var activeScenario: ActiveScreenshotScenario

    private val screenshotDevice = ScreenshotDevice()
    private val instrumentation = screenshotDevice.instrumentation
    private val device = screenshotDevice.ui
    private val appPackage = screenshotDevice.appPackage
    private val widgetManager = AppWidgetManager.getInstance(screenshotDevice.targetContext)
    private val widgetProvider = ComponentName(appPackage, "com.w2sv.widget.WifiWidgetProvider")
    private val launcher = LauncherWorkspace(screenshotDevice, widgetManager, widgetProvider)
    private val wallpaper = ScreenshotWallpaper(screenshotDevice)
    private val widgetController = LauncherWidgetController(screenshotDevice, widgetManager, widgetProvider)

    @Test
    fun `capture widget screenshots for each scenario`() {
        logProgress("Initializing widget screenshot infrastructure category=${screenshotDevice.category}")
        initializeTestInfrastructure()
        logProgress("Preparing widget screenshot launcher category=${screenshotDevice.category}")
        prepareDeviceAndLauncher()
        ScreenshotScenarios.all.forEach(::captureScenario)
        logProgress("Completed widget screenshot scenarios category=${screenshotDevice.category}")
    }

    private fun initializeTestInfrastructure() {
        hiltRule.inject()
        if (runCatching { WorkManager.getInstance(instrumentation.targetContext) }.isFailure) {
            WorkManager.initialize(
                instrumentation.targetContext,
                Configuration.Builder().setWorkerFactory(workerFactory).build()
            )
        }
    }

    private fun prepareDeviceAndLauncher() {
        ScreenshotDeviceSetup(screenshotDevice).prepare()
        launcher.prepare()
    }

    private fun captureScenario(scenario: ScreenshotScenario) {
        logProgress("Clearing launcher widgets scenario=${scenario.name}")
        launcher.clearExistingWidgets()
        logProgress("Applying wallpaper scenario=${scenario.name}")
        wallpaper.set(scenario.wallpaperRes)
        logProgress("Selecting scenario and configuration scenario=${scenario.name}")
        activeScenario.select(scenario)
        persistScenarioConfig(scenario)

        logProgress("Pinning and resizing widget scenario=${scenario.name}")
        val widgetId = widgetController.pin()
        widgetController.resize(scenario.widgetSizes.forDevice(screenshotDevice.category))
        logProgress("Verifying launcher widget scenario=${scenario.name} widgetId=$widgetId")
        device.waitForIdle()
        check(widgetManager.getAppWidgetIds(widgetProvider).toSet() == setOf(widgetId)) {
            "Expected exactly one widget for ${scenario.name}, ID $widgetId. ${screenshotDevice.hierarchy()}"
        }
        check(device.findObjects(By.res(appPackage, "widget_layout")).size == 1) {
            "Expected exactly one launcher-visible widget for ${scenario.name}. ${screenshotDevice.hierarchy()}"
        }
        launcher.checkDockEmpty()
        check(!device.hasObject(By.pkg(LAUNCHER_PACKAGE).text("ADD TO HOME SCREEN"))) {
            "Launcher pin dialog is still visible. ${screenshotDevice.hierarchy()}"
        }
        check(!device.hasObject(By.res(appPackage, "loading_tv"))) {
            "The widget is still showing its loading view. ${screenshotDevice.hierarchy()}"
        }
        check(
            device.hasObject(By.res(appPackage, "last_updated_tv")) ==
                (WidgetUtility.RefreshTimeDisplay in scenario.enabledUtilities)
        ) {
            "Unexpected refresh timestamp visibility for ${scenario.name}. ${screenshotDevice.hierarchy()}"
        }

        val systemUi = ScreenshotSystemUi(screenshotDevice)
        try {
            logProgress("Capturing launcher screenshot scenario=${scenario.name}")
            systemUi.enterDemoMode()
            screenshotDevice.captureScreenshot(scenario.name)
        } finally {
            systemUi.exitDemoMode()
        }
        logProgress("Completed launcher screenshot scenario=${scenario.name}")
    }

    private fun persistScenarioConfig(scenario: ScreenshotScenario) {
        val expectedConfig = scenario.widgetConfig
        runBlocking {
            widgetConfigDataSource.update { expectedConfig }
            widgetConfigSource.flow.first { it == expectedConfig }
            val persisted = widgetConfigSource.snapshot()
            check(persisted == expectedConfig && persisted.enabledProperties == scenario.enabledProperties) {
                "The screenshot widget configuration for ${scenario.name} was not persisted: $persisted"
            }
        }
    }
}

private fun logProgress(message: String) {
    println("Screenshot Gen: $message")
}
