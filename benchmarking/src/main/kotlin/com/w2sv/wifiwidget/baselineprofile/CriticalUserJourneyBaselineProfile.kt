package com.w2sv.wifiwidget.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
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
    closeLocationAccessPermissionRationalAndRequestDialogIfOpen()

    goToWidgetConfigurationScreen()
    waitForIdle()

    pressBack()
}

private fun UiDevice.closeLocationAccessPermissionRationalAndRequestDialogIfOpen() {
    // 'Understood' button is part of the location access rational dialog
    findObject(By.text("Understood"))?.let {
        it.click() // Closes the rational dialog
        waitForIdle() // Wait until location access permission request dialog appears
        pressBack() // Closes location access permission request dialog
    }
}

private fun UiDevice.goToWidgetConfigurationScreen() {
    findObject(By.text("Configure")).click()
}
