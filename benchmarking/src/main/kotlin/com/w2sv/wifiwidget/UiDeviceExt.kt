package com.w2sv.wifiwidget

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import java.io.ByteArrayOutputStream

internal fun UiDevice.criticalUserJourney() {
    closeLocationAccessPermissionRationalAndRequestDialogIfOpen()
    flingListDown("scrollableWifiPropertyList")

    goToWidgetConfigurationScreen()
    waitForIdle()

    flingListDown("scrollableWidgetConfigurationColumn")

    pressBack()
}

private fun UiDevice.closeLocationAccessPermissionRationalAndRequestDialogIfOpen() {
    findObject(By.text("Understood"))?.let { // 'Understood' button is part of the location access rational dialog
        it.click() // Closes the rational dialog
        waitForIdle() // Wait until location access permission request dialog appears
        pressBack() // Closes location access permission request dialog
    }
}

private fun UiDevice.dumpWindowHierarchy(): String {
    val outputStream = ByteArrayOutputStream()
    dumpWindowHierarchy(outputStream)
    return outputStream.toString("UTF-8")
}

private fun UiDevice.flingListDown(resourceName: String) {
    findObject(By.res(resourceName)).fling(Direction.DOWN)
    waitForIdle()
}

private fun UiDevice.goToWidgetConfigurationScreen() {
    findObject(UiSelector().description("Open the widget configuration screen.")).click()
}
