package com.w2sv.wifiwidget.util

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import java.io.ByteArrayOutputStream

internal fun UiDevice.flingObject(resourceName: String, direction: Direction) {
    findObject(By.res(resourceName)).fling(direction)
    waitForIdle()
}

internal fun UiDevice.dumpWindowHierarchy(): String {
    val outputStream = ByteArrayOutputStream()
    dumpWindowHierarchy(outputStream)
    return outputStream.toString("UTF-8")
}
