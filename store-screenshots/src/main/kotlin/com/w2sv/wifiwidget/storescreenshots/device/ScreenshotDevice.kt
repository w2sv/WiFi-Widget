package com.w2sv.wifiwidget.storescreenshots.device

import android.app.Instrumentation
import android.content.Context
import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.io.PlatformTestStorageRegistry
import com.w2sv.wifiwidget.testing.launcher.LauncherDevice

internal const val LAUNCHER_PACKAGE = com.w2sv.wifiwidget.testing.launcher.LAUNCHER_PACKAGE
internal const val SYSTEM_UI_PACKAGE = com.w2sv.wifiwidget.testing.launcher.SYSTEM_UI_PACKAGE
internal const val SYSTEM_TIMEOUT_MS = com.w2sv.wifiwidget.testing.launcher.SYSTEM_TIMEOUT_MS
internal const val UI_TIMEOUT_MS = com.w2sv.wifiwidget.testing.launcher.UI_TIMEOUT_MS
internal const val POLL_INTERVAL_MS = com.w2sv.wifiwidget.testing.launcher.POLL_INTERVAL_MS

/** Shared access to the managed emulator and its launcher/System UI diagnostics. */
internal class ScreenshotDevice {
    val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
    val targetContext: Context = instrumentation.targetContext
    val launcher = LauncherDevice(instrumentation, targetContext)
    val ui = launcher.ui
    val appPackage: String = targetContext.packageName
    val smallestWidthDp = targetContext.resources.configuration.smallestScreenWidthDp
    val category = when {
        smallestWidthDp >= LARGE_TABLET_MIN_WIDTH_DP -> ScreenshotDeviceCategory.LargeTablet
        smallestWidthDp >= TABLET_MIN_WIDTH_DP -> ScreenshotDeviceCategory.Tablet
        else -> ScreenshotDeviceCategory.Phone
    }
    val screenshotDirectory get() = category.screenshotDirectory

    fun captureScreenshot(filename: String) {
        val screenshot = checkNotNull(instrumentation.uiAutomation.takeScreenshot()) {
            "UiAutomation did not capture the display."
        }
        PlatformTestStorageRegistry.getInstance()
            .openOutputFile("$screenshotDirectory/$filename")
            .use { output ->
                check(screenshot.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    "Could not write $filename."
                }
            }
    }

    fun executeShellCommand(command: String): String =
        launcher.executeShellCommand(command)

    fun hierarchy(): String =
        launcher.hierarchy()

    fun waitForLauncherToSettle() =
        launcher.waitForLauncherToSettle(
            requireHotseat = category == ScreenshotDeviceCategory.LargeTablet
        )

    private companion object {
        const val TABLET_MIN_WIDTH_DP = 600
        const val LARGE_TABLET_MIN_WIDTH_DP = 720
    }
}
