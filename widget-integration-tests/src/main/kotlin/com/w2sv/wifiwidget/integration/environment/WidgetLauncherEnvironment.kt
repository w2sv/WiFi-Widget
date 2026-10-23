package com.w2sv.wifiwidget.integration.environment

import android.app.Instrumentation
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.ParcelFileDescriptor
import androidx.test.uiautomator.By
import com.w2sv.wifiwidget.testing.launcher.GridCells
import com.w2sv.wifiwidget.testing.launcher.LauncherDevice
import com.w2sv.wifiwidget.testing.launcher.LauncherFormFactor
import com.w2sv.wifiwidget.testing.launcher.LauncherSizing
import com.w2sv.wifiwidget.testing.launcher.LauncherWidgetController
import com.w2sv.wifiwidget.testing.launcher.LauncherWorkspace

/** Owns the launcher widget and its visible Android UI for one integration test. */
class WidgetLauncherEnvironment(
    private val instrumentation: Instrumentation,
    private val context: Context,
    private val diagnostics: () -> String
) {
    private val launcher = LauncherDevice(instrumentation, context)
    private val device = launcher.ui
    private val appPackage = launcher.appPackage
    private val workspace = LauncherWorkspace(launcher, LauncherFormFactor.Phone)
    private val widgetController = LauncherWidgetController(
        launcher,
        LauncherSizing(LauncherFormFactor.Phone, GridCells(5, 5), GridCells(3, 2), 24)
    )
    private var pinnedWidgetId: Int? = null

    fun prepare() {
        showLauncher()
        clearExistingWidgets()
    }

    fun showLauncher() {
        launcher.showLauncher()
    }

    fun clearExistingWidgets() {
        workspace.clearExistingWidgets()
    }

    fun pinWidget(size: GridCells? = null): Int {
        val id = widgetController.pin()
        pinnedWidgetId = id
        if (size != null) widgetController.resize(size)
        return id
    }

    fun widgetIds(): Set<Int> =
        launcher.widgetIds()

    fun awaitWidgetText(value: String) {
        showPinnedWidget()
        await("launcher widget text '$value'") {
            device.hasObject(By.res(appPackage, "widget_layout")) &&
                device.hasObject(By.res(appPackage, "property_value_tv").text(value))
        }
    }

    fun widgetHasText(value: String): Boolean =
        device.hasObject(By.res(appPackage, "property_value_tv").text(value))

    fun widgetHasLabel(value: String): Boolean =
        device.hasObject(By.res(appPackage, "property_label_tv").text(value))

    fun widgetLabels(): List<String> =
        device.findObjects(By.res(appPackage, "property_label_tv"))
            .sortedBy { it.visibleBounds.top }
            .mapNotNull { it.text }

    fun widgetHasResource(resource: String): Boolean =
        device.hasObject(By.res(appPackage, resource))

    fun triggerWidgetUpdate(id: Int) {
        context.sendBroadcast(
            Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .setComponent(launcher.widgetProvider)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(id))
        )
    }

    fun awaitBroadcastIdle() {
        val descriptor = instrumentation.uiAutomation.executeShellCommand("am wait-for-broadcast-idle")
        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
    }

    fun await(
        description: String,
        timeoutMs: Long = 30_000,
        condition: () -> Boolean
    ) =
        launcher.await(description, timeoutMs, diagnostics = diagnostics, condition = condition)

    fun diagnostics(): String =
        "widgetIds=${widgetIds()}, hierarchy=${launcher.hierarchy()}"

    private fun showPinnedWidget() {
        val id = checkNotNull(pinnedWidgetId) { "No widget was pinned by this test." }
        launcher.showLauncher()
        check(launcher.widgetIds() == setOf(id)) {
            "Expected only pinned widget $id, found ${launcher.widgetIds()}. ${launcher.hierarchy()}"
        }
        launcher.await("widget $id visible on a launcher page") { launcher.findWidgetOnPages() != null }
    }
}
