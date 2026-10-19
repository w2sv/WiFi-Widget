package com.w2sv.wifiwidget.storescreenshots.device

import android.app.UiModeManager
import android.os.SystemClock
import android.text.format.DateFormat
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until

/** Prepares the managed Pixel 6 before Launcher3 places or measures the widget. */
internal class ScreenshotDeviceSetup(private val screenshotDevice: ScreenshotDevice) {
    private val context = screenshotDevice.targetContext
    private val device = screenshotDevice.ui

    fun prepare() {
        disableAnimations()
        ensureLightMode()
        ensureDefaultFontScale()
        ensureNativeDisplay()
        ensureEnglishUnitedStates()
        ensureTwelveHourClock()
        enableGestureNavigation()

        screenshotDevice.executeShellCommand("am wait-for-broadcast-idle")
        device.pressHome()
        check(device.wait(Until.hasObject(By.pkg(LAUNCHER_PACKAGE)), SYSTEM_TIMEOUT_MS)) {
            "Launcher3 did not become visible after navigation setup. ${screenshotDevice.hierarchy()}"
        }
        check(device.wait(Until.hasObject(By.res(SYSTEM_UI_PACKAGE, "status_bar")), SYSTEM_TIMEOUT_MS)) {
            "System UI status bar did not become accessible after navigation setup. ${screenshotDevice.hierarchy()}"
        }
        screenshotDevice.waitForLauncherToSettle()
        check(
            !device.hasObject(By.res(SYSTEM_UI_PACKAGE, "back")) &&
                !device.hasObject(By.res(SYSTEM_UI_PACKAGE, "home")) &&
                !device.hasObject(By.res(SYSTEM_UI_PACKAGE, "recent_apps"))
        ) { "System UI still exposes three-button navigation after enabling the gestural overlay. ${screenshotDevice.hierarchy()}" }
    }

    private fun disableAnimations() {
        listOf("window_animation_scale", "transition_animation_scale", "animator_duration_scale").forEach { setting ->
            if (screenshotDevice.executeShellCommand("settings get global $setting").toFloatOrNull() != 0f) {
                screenshotDevice.executeShellCommand("settings put global $setting 0")
            }
            check(screenshotDevice.executeShellCommand("settings get global $setting").toFloatOrNull() == 0f) {
                "Could not disable $setting on the managed emulator."
            }
        }
    }

    private fun ensureLightMode() {
        val uiModeManager = context.getSystemService(UiModeManager::class.java)
        if (uiModeManager.nightMode != UiModeManager.MODE_NIGHT_NO) {
            screenshotDevice.executeShellCommand("cmd uimode night no")
        }
        waitForCondition("light mode") { uiModeManager.nightMode == UiModeManager.MODE_NIGHT_NO }
    }

    private fun ensureDefaultFontScale() {
        if (context.resources.configuration.fontScale != 1f) {
            screenshotDevice.executeShellCommand("settings put system font_scale 1.0")
        }
        waitForCondition("font scale 1.0") { context.resources.configuration.fontScale == 1f }
    }

    private fun ensureNativeDisplay() {
        if (screenshotDevice.executeShellCommand("wm size").contains("Override size:")) {
            screenshotDevice.executeShellCommand("wm size reset")
        }
        if (screenshotDevice.executeShellCommand("wm density").contains("Override density:")) {
            screenshotDevice.executeShellCommand("wm density reset")
        }
        check(!screenshotDevice.executeShellCommand("wm size").contains("Override size:")) {
            "The managed emulator still has a display-size override."
        }
        check(!screenshotDevice.executeShellCommand("wm density").contains("Override density:")) {
            "The managed emulator still has a display-density override."
        }
    }

    private fun ensureEnglishUnitedStates() {
        val locale = context.resources.configuration.locales[0]
        check(locale.toLanguageTag() == "en-US") {
            "The managed emulator locale is ${locale.toLanguageTag()}, expected en-US; automatic locale switching is disabled."
        }
    }

    private fun ensureTwelveHourClock() {
        if (DateFormat.is24HourFormat(context)) {
            screenshotDevice.executeShellCommand("settings put system time_12_24 12")
        }
        waitForCondition("12-hour clock") { !DateFormat.is24HourFormat(context) }
    }

    private fun enableGestureNavigation() {
        val before = screenshotDevice.executeShellCommand("cmd overlay list --user 0")
        val overlay = before.lineSequence().firstOrNull { GESTURAL_OVERLAY in it }
        check(overlay != null) {
            "The managed Pixel 6 has no gestural navigation overlay. cmd overlay list --user 0: $before"
        }
        if (!overlay.trimStart().startsWith("[x]")) {
            screenshotDevice.executeShellCommand("cmd overlay enable-exclusive --user 0 --category $GESTURAL_OVERLAY")
        }
        val after = screenshotDevice.executeShellCommand("cmd overlay list --user 0")
        check(after.lineSequence().any { GESTURAL_OVERLAY in it && it.trimStart().startsWith("[x]") }) {
            "The gestural navigation overlay did not become enabled. Before: $before After: $after"
        }
    }

    private fun waitForCondition(description: String, condition: () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + SYSTEM_TIMEOUT_MS
        while (SystemClock.elapsedRealtime() < deadline) {
            if (condition()) return
            SystemClock.sleep(POLL_INTERVAL_MS)
        }
        error("The managed emulator did not reach $description.")
    }

    private companion object {
        const val GESTURAL_OVERLAY = "com.android.internal.systemui.navbar.gestural"
    }
}
