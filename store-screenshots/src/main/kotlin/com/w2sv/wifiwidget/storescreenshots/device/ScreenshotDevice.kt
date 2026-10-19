package com.w2sv.wifiwidget.storescreenshots.device

import android.app.Instrumentation
import android.content.Context
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.io.PlatformTestStorageRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import java.io.ByteArrayOutputStream

internal const val LAUNCHER_PACKAGE = "com.android.launcher3"
internal const val SYSTEM_UI_PACKAGE = "com.android.systemui"
internal const val SYSTEM_TIMEOUT_MS = 30_000L
internal const val UI_TIMEOUT_MS = 20_000L
internal const val POLL_INTERVAL_MS = 200L

/** Shared access to the managed emulator and its launcher/System UI diagnostics. */
internal class ScreenshotDevice {
    val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
    val ui = UiDevice.getInstance(instrumentation)
    val targetContext: Context = instrumentation.targetContext
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
        ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command)).use {
            it.bufferedReader().readText().trim()
        }

    fun hierarchy(): String =
        ByteArrayOutputStream().use { output ->
            ui.dumpWindowHierarchy(output)
            output.toString("UTF-8")
        }

    fun waitForLauncherToSettle() {
        val deadline = SystemClock.elapsedRealtime() + SYSTEM_TIMEOUT_MS
        var stableProcessIds = ""
        var stableSince = 0L
        while (SystemClock.elapsedRealtime() < deadline) {
            val processIds = listOf(
                executeShellCommand("pidof $LAUNCHER_PACKAGE"),
                executeShellCommand("pidof $SYSTEM_UI_PACKAGE")
            )
            val currentProcessIds = processIds.joinToString()
            val now = SystemClock.elapsedRealtime()
            if (
                processIds.none(String::isBlank) &&
                ui.hasObject(By.res(LAUNCHER_PACKAGE, "workspace")) &&
                ui.hasObject(By.res(SYSTEM_UI_PACKAGE, "status_bar")) &&
                !isAnrDialogVisible &&
                isLauncherDockReady
            ) {
                if (currentProcessIds != stableProcessIds) {
                    stableProcessIds = currentProcessIds
                    stableSince = now
                } else if (now - stableSince >= SYSTEM_QUIET_PERIOD_MS) {
                    ui.waitForIdle()
                    return
                }
            } else {
                stableProcessIds = ""
                stableSince = 0L
            }
            SystemClock.sleep(POLL_INTERVAL_MS)
        }
        error("Launcher3 and System UI did not settle during screenshot preparation. ${hierarchy()}")
    }

    private val isAnrDialogVisible: Boolean
        get() = instrumentation.uiAutomation.rootInActiveWindow
            ?.findAccessibilityNodeInfosByText("isn't responding")
            .orEmpty()
            .isNotEmpty()

    private val isLauncherDockReady: Boolean
        get() = when (category) {
            ScreenshotDeviceCategory.Phone,
            ScreenshotDeviceCategory.Tablet -> true
            // This AOSP Pixel Tablet profile exposes an empty hotseat and no pinned-app labels.
            ScreenshotDeviceCategory.LargeTablet -> ui.hasObject(By.res(LAUNCHER_PACKAGE, "hotseat"))
        }

    private companion object {
        const val TABLET_MIN_WIDTH_DP = 600
        const val LARGE_TABLET_MIN_WIDTH_DP = 720
        const val SYSTEM_QUIET_PERIOD_MS = 5_000L
    }
}
