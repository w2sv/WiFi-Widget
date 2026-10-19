package com.w2sv.wifiwidget.storescreenshots.device

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until

/** Applies and restores AOSP System UI Demo Mode around a single launcher capture. */
internal class ScreenshotSystemUi(private val screenshotDevice: ScreenshotDevice) {
    private val device = screenshotDevice.ui

    fun enterDemoMode() {
        if (screenshotDevice.executeShellCommand("settings get global sysui_demo_allowed") != "1") {
            screenshotDevice.executeShellCommand("settings put global sysui_demo_allowed 1")
        }
        check(screenshotDevice.executeShellCommand("settings get global sysui_demo_allowed") == "1") {
            "System UI Demo Mode could not be enabled on the managed emulator."
        }
        screenshotDevice.executeShellCommand("settings put global sysui_tuner_demo_on 1")
        val demoClock = By.res(SYSTEM_UI_PACKAGE, "clock").text("10:08")
        var clockReady = false
        for (attempt in 1..DEMO_MODE_ATTEMPTS) {
            broadcast("enter")
            broadcast("clock -e hhmm 1008")
            broadcast("battery -e level 100 -e plugged false")
            broadcast("network -e wifi show -e level 4 -e fully true")
            broadcast("network -e mobile hide")
            broadcast("notifications -e visible false")
            if (device.wait(Until.hasObject(demoClock), DEMO_MODE_RETRY_MS)) {
                clockReady = true
                break
            }
        }
        check(clockReady) {
            "Demo Mode clock did not show 10:08. ${screenshotDevice.hierarchy()}"
        }
        device.waitForIdle()
        val battery = checkNotNull(device.findObject(By.res(SYSTEM_UI_PACKAGE, "battery"))) {
            "Demo Mode battery indicator is missing. ${screenshotDevice.hierarchy()}"
        }.contentDescription.orEmpty()
        check("100" in battery && !battery.contains("charging", ignoreCase = true)) {
            "Demo Mode battery is not full and unplugged: '$battery'. ${screenshotDevice.hierarchy()}"
        }
        val wifiSignal = By.res(SYSTEM_UI_PACKAGE, "wifi_signal")
        var wifiReady = device.wait(Until.hasObject(wifiSignal), DEMO_MODE_RETRY_MS)
        for (attempt in 2..DEMO_MODE_ATTEMPTS) {
            if (wifiReady) break
            // AOSP System UI can drop network commands while entering Demo Mode.
            broadcast("network -e wifi show -e level 4 -e fully true")
            wifiReady = device.wait(Until.hasObject(wifiSignal), DEMO_MODE_RETRY_MS)
        }
        val description = checkNotNull(device.findObject(wifiSignal)) {
            "Demo Mode wifi_signal indicator is missing. ${screenshotDevice.hierarchy()}"
        }.contentDescription.orEmpty()
        check(description.contains("full", ignoreCase = true)) {
            "Demo Mode wifi_signal indicator is not full strength: '$description'. ${screenshotDevice.hierarchy()}"
        }
        check(!device.hasObject(By.pkg(SYSTEM_UI_PACKAGE).descContains("notification:"))) {
            "Demo Mode still exposes notification icons. ${screenshotDevice.hierarchy()}"
        }
    }

    fun exitDemoMode() {
        try {
            broadcast("exit")
        } finally {
            screenshotDevice.executeShellCommand("settings put global sysui_tuner_demo_on 0")
        }
    }

    private fun broadcast(command: String) {
        val output = screenshotDevice.executeShellCommand("am broadcast -a com.android.systemui.demo -e command $command")
        check("Broadcast completed" in output) {
            "System UI Demo Mode command '$command' failed: $output"
        }
    }

    private companion object {
        const val DEMO_MODE_ATTEMPTS = 4
        const val DEMO_MODE_RETRY_MS = 5_000L
    }
}
