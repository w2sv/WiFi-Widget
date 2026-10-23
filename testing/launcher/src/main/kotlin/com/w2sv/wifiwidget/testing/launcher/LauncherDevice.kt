package com.w2sv.wifiwidget.testing.launcher

import android.app.Instrumentation
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import java.io.ByteArrayOutputStream

const val LAUNCHER_PACKAGE = "com.android.launcher3"
const val SYSTEM_UI_PACKAGE = "com.android.systemui"
const val SYSTEM_TIMEOUT_MS = 30_000L
const val UI_TIMEOUT_MS = 20_000L
const val POLL_INTERVAL_MS = 200L

/** Launcher3 access and diagnostics shared by managed-device widget tests. */
class LauncherDevice(
    val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation(),
    targetContext: Context = instrumentation.targetContext
) {
    val ui: UiDevice = UiDevice.getInstance(instrumentation)
    val appPackage: String = targetContext.packageName
    val widgetManager: AppWidgetManager = AppWidgetManager.getInstance(targetContext)
    val widgetProvider = ComponentName(appPackage, "com.w2sv.widget.WifiWidgetProvider")

    fun widgetIds(): Set<Int> =
        widgetManager.getAppWidgetIds(widgetProvider).toSet()

    fun hierarchy(): String =
        ByteArrayOutputStream().use { output ->
            ui.dumpWindowHierarchy(output)
            output.toString("UTF-8")
        }

    fun executeShellCommand(command: String): String =
        ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command)).use {
            it.bufferedReader().readText().trim()
        }

    fun await(
        description: String,
        timeoutMs: Long = UI_TIMEOUT_MS,
        diagnostics: () -> String = { "widgetIds=${widgetIds()}. ${hierarchy()}" },
        condition: () -> Boolean
    ) {
        val result = poll(timeoutMs, condition)
        if (result.satisfied) return
        error("Timed out waiting for $description; lastStaleView=${result.lastStaleView}. ${diagnostics()}")
    }

    /** Use [await] when a timeout should fail with launcher and stale-view diagnostics. */
    fun pollUntil(timeoutMs: Long = UI_TIMEOUT_MS, condition: () -> Boolean): Boolean =
        poll(timeoutMs, condition).satisfied

    private fun poll(timeoutMs: Long, condition: () -> Boolean): PollResult {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        var lastStaleView: StaleObjectException? = null
        while (SystemClock.elapsedRealtime() < deadline) {
            try {
                if (condition()) return PollResult(true, lastStaleView)
            } catch (exception: StaleObjectException) {
                lastStaleView = exception
            }
            SystemClock.sleep(POLL_INTERVAL_MS)
        }
        return PollResult(false, lastStaleView)
    }

    fun <T : Any> awaitValue(
        description: String,
        timeoutMs: Long = UI_TIMEOUT_MS,
        value: () -> T?
    ): T {
        var result: T? = null
        await(description, timeoutMs) { value()?.also { result = it } != null }
        return checkNotNull(result)
    }

    fun showLauncher() {
        if (!ui.hasObject(By.res(LAUNCHER_PACKAGE, "workspace"))) ui.pressHome()
        await("Launcher3 workspace") { ui.hasObject(By.res(LAUNCHER_PACKAGE, "workspace")) }
        ui.waitForIdle()
    }

    /** Avoids assuming that Home returns to the page where a widget was pinned. */
    fun findWidgetOnPages(): UiObject2? {
        ui.findObject(By.res(appPackage, "widget_layout"))?.let { return it }
        repeat(MAX_PAGE_MOVES) {
            val workspace = ui.findObject(By.res(LAUNCHER_PACKAGE, "workspace")) ?: return null
            workspace.scroll(Direction.LEFT, 0.9f)
            ui.findObject(By.res(appPackage, "widget_layout"))?.let { return it }
        }
        repeat(MAX_PAGE_MOVES * 2) {
            val workspace = ui.findObject(By.res(LAUNCHER_PACKAGE, "workspace")) ?: return null
            workspace.scroll(Direction.RIGHT, 0.9f)
            ui.findObject(By.res(appPackage, "widget_layout"))?.let { return it }
        }
        return null
    }

    /** Screenshot setup needs a quiet launcher; behavioral tests can use [showLauncher]. */
    fun waitForLauncherToSettle(requireHotseat: Boolean = false) {
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
                (!requireHotseat || ui.hasObject(By.res(LAUNCHER_PACKAGE, "hotseat")))
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
        error("Launcher3 and System UI did not settle. ${hierarchy()}")
    }

    private val isAnrDialogVisible: Boolean
        get() = instrumentation.uiAutomation.rootInActiveWindow
            ?.findAccessibilityNodeInfosByText("isn't responding")
            .orEmpty()
            .isNotEmpty()

    private companion object {
        const val MAX_PAGE_MOVES = 5
        const val SYSTEM_QUIET_PERIOD_MS = 5_000L
    }

    private data class PollResult(val satisfied: Boolean, val lastStaleView: StaleObjectException?)
}
