package com.w2sv.wifiwidget.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.w2sv.wifiwidget.util.dumpWindowHierarchy
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class CriticalUserJourneyBaselineProfile {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(packageName = "com.w2sv.wifiwidget") {
            startActivityAndWait()
            device.criticalUserJourney()
        }
    }
}

private fun UiDevice.criticalUserJourney() {
    waitForIdle()
    skipLocationOnboardingIfOpen()

    goToWidgetConfigurationScreen()
    waitForIdle()

    pressBack()
}

private fun UiDevice.skipLocationOnboardingIfOpen() {
    if (findObject(By.text("Configure")) != null) return
    wait(Until.findObject(By.text("Continue without location access")), 5_000)?.let {
        it.click()
        waitForIdle()
    }
}

private fun UiDevice.goToWidgetConfigurationScreen() {
    val configureButton = wait(Until.findObject(By.text("Configure")), 5_000)
        ?: error("Configure button not found:\n${dumpWindowHierarchy()}")
    configureButton.click()
}
