package com.w2sv.wifiwidget.storescreenshots.launcher

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.Point
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.w2sv.wifiwidget.storescreenshots.device.LAUNCHER_PACKAGE
import com.w2sv.wifiwidget.storescreenshots.device.POLL_INTERVAL_MS
import com.w2sv.wifiwidget.storescreenshots.device.SYSTEM_UI_PACKAGE
import com.w2sv.wifiwidget.storescreenshots.device.ScreenshotDevice
import com.w2sv.wifiwidget.storescreenshots.device.ScreenshotDeviceCategory
import com.w2sv.wifiwidget.storescreenshots.device.UI_TIMEOUT_MS

/** Establishes the empty Launcher3 workspace for widget screenshots. */
internal class LauncherWorkspace(
    private val screenshotDevice: ScreenshotDevice,
    private val widgetManager: AppWidgetManager,
    private val widgetProvider: ComponentName
) {
    private val instrumentation = screenshotDevice.instrumentation
    private val device = screenshotDevice.ui
    private val appPackage = screenshotDevice.appPackage

    fun prepare() {
        screenshotDevice.waitForLauncherToSettle()
        clearDock()
        screenshotDevice.waitForLauncherToSettle()
        checkDockEmpty()
    }

    private fun clearDock() {
        repeat(MAX_DOCK_REMOVALS) {
            val hotseat = checkNotNull(device.wait(Until.findObject(By.res(LAUNCHER_PACKAGE, "hotseat")), UI_TIMEOUT_MS)) {
                "Launcher3 hotseat is unavailable. ${screenshotDevice.hierarchy()}"
            }
            val shortcut = hotseat.findObjects(By.clazz("android.widget.TextView")).firstOrNull() ?: return
            val label = shortcut.text
            removeDockShortcut(shortcut)
            check(device.wait(Until.hasObject(By.res(LAUNCHER_PACKAGE, "label").text("Item removed")), UI_TIMEOUT_MS)) {
                "Launcher3 did not remove dock shortcut $label. ${screenshotDevice.hierarchy()}"
            }
            check(device.wait(Until.gone(By.res(LAUNCHER_PACKAGE, "label")), UI_TIMEOUT_MS)) {
                "Launcher3 removal message did not clear. ${screenshotDevice.hierarchy()}"
            }
        }
        checkDockEmpty()
    }

    fun clearExistingWidgets() {
        var removalAttempts = 0
        while (true) {
            val existingIds = widgetManager.getAppWidgetIds(widgetProvider).toSet()
            if (existingIds.isEmpty()) break
            check(removalAttempts < MAX_WIDGET_REMOVAL_ATTEMPTS) {
                "Launcher3 still hosts WifiWidgetProvider IDs $existingIds after $MAX_WIDGET_REMOVAL_ATTEMPTS removal attempts. " +
                    screenshotDevice.hierarchy()
            }
            val widget = checkNotNull(device.wait(Until.findObject(By.res(appPackage, "widget_layout")), UI_TIMEOUT_MS)) {
                "Existing WifiWidgetProvider IDs $existingIds are not visible on this launcher page. ${screenshotDevice.hierarchy()}"
            }
            removeWidget(widget)
            removalAttempts++
            if (!waitForWidgetIdRemoval(existingIds)) {
                device.pressBack()
                screenshotDevice.waitForLauncherToSettle()
                continue
            }
            check(device.wait(Until.gone(By.res(LAUNCHER_PACKAGE, "label")), UI_TIMEOUT_MS)) {
                "Launcher3 widget removal message did not clear. ${screenshotDevice.hierarchy()}"
            }
        }
        check(widgetManager.getAppWidgetIds(widgetProvider).isEmpty()) {
            "WifiWidgetProvider IDs remain after launcher cleanup. ${screenshotDevice.hierarchy()}"
        }
        check(device.wait(Until.gone(By.res(appPackage, "widget_layout")), UI_TIMEOUT_MS)) {
            "An old widget view remains after its IDs were removed. ${screenshotDevice.hierarchy()}"
        }
    }

    private fun waitForWidgetIdRemoval(previousIds: Set<Int>): Boolean {
        val deadline = SystemClock.elapsedRealtime() + UI_TIMEOUT_MS
        while (SystemClock.elapsedRealtime() < deadline) {
            if (!widgetManager.getAppWidgetIds(widgetProvider).toSet().containsAll(previousIds)) return true
            SystemClock.sleep(POLL_INTERVAL_MS)
        }
        return false
    }

    fun checkDockEmpty() {
        val hotseat = checkNotNull(device.wait(Until.findObject(By.res(LAUNCHER_PACKAGE, "hotseat")), UI_TIMEOUT_MS)) {
            "Launcher3 hotseat is unavailable. ${screenshotDevice.hierarchy()}"
        }
        check(hotseat.findObjects(By.clazz("android.widget.TextView")).isEmpty()) {
            "Launcher3 dock contains shortcuts or suggestions. ${screenshotDevice.hierarchy()}"
        }
    }

    private fun removeDockShortcut(shortcut: UiObject2) {
        when (screenshotDevice.category) {
            ScreenshotDeviceCategory.Phone -> dragPhoneItemToRemove(shortcut)
            ScreenshotDeviceCategory.Tablet,
            ScreenshotDeviceCategory.LargeTablet -> removeTabletDockShortcut(shortcut)
        }
    }

    private fun removeWidget(widget: UiObject2) {
        when (screenshotDevice.category) {
            ScreenshotDeviceCategory.Phone -> dragPhoneItemToRemove(widget)
            ScreenshotDeviceCategory.Tablet,
            ScreenshotDeviceCategory.LargeTablet -> removeTabletWidget()
        }
    }

    private fun removeTabletDockShortcut(shortcut: UiObject2) {
        val label = shortcut.text
        val accessibilityNode = instrumentation.uiAutomation.rootInActiveWindow
            ?.findAccessibilityNodeInfosByText(label)
            ?.firstOrNull { it.text?.toString() == label }
        val moveAction = accessibilityNode?.actionList?.firstOrNull { it.label?.toString() == "Move item" }
        check(moveAction != null && accessibilityNode.performAction(moveAction.id)) {
            "Launcher3 did not start moving dock shortcut $label. ${screenshotDevice.hierarchy()}"
        }
        val removeTarget = checkNotNull(
            device.wait(Until.findObject(By.res(LAUNCHER_PACKAGE, "delete_target_text")), UI_TIMEOUT_MS)
        ) { "Launcher3 did not expose a Remove target. ${screenshotDevice.hierarchy()}" }
        removeTarget.click()
    }

    private fun removeTabletWidget() {
        val root = checkNotNull(instrumentation.uiAutomation.rootInActiveWindow) {
            "Launcher3 accessibility tree is unavailable. ${screenshotDevice.hierarchy()}"
        }
        val host = checkNotNull(findWidgetHost(root)) {
            "Launcher3 widget host is unavailable. ${screenshotDevice.hierarchy()}"
        }
        val moveAction = host.actionList.firstOrNull { it.label?.toString() == "Move item" }
        check(moveAction != null && host.performAction(moveAction.id)) {
            "Launcher3 widget host has no usable Move item action: ${host.actionList}. ${screenshotDevice.hierarchy()}"
        }
        val removeTarget = checkNotNull(
            device.wait(Until.findObject(By.res(LAUNCHER_PACKAGE, "delete_target_text")), UI_TIMEOUT_MS)
        ) { "Launcher3 did not expose a widget Remove target. ${screenshotDevice.hierarchy()}" }
        val clickedWithAccessibility = instrumentation.uiAutomation.rootInActiveWindow
            ?.findAccessibilityNodeInfosByViewId("$LAUNCHER_PACKAGE:id/delete_target_text")
            ?.firstOrNull()
            ?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
        if (!clickedWithAccessibility) removeTarget.click()
    }

    private fun findWidgetHost(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.className?.toString() == "com.android.launcher3.widget.LauncherAppWidgetHostView") return node
        for (index in 0 until node.childCount) {
            node.getChild(index)?.let { child -> findWidgetHost(child)?.let { return it } }
        }
        return null
    }

    private fun dragPhoneItemToRemove(item: UiObject2) {
        val workspaceBounds = checkNotNull(device.findObject(By.res(LAUNCHER_PACKAGE, "workspace"))) {
            "Launcher3 workspace is unavailable. ${screenshotDevice.hierarchy()}"
        }.visibleBounds
        val statusBarBounds = checkNotNull(device.findObject(By.res(SYSTEM_UI_PACKAGE, "status_bar"))) {
            "System UI status bar is unavailable. ${screenshotDevice.hierarchy()}"
        }.visibleBounds
        val removeTarget = Point(workspaceBounds.centerX(), statusBarBounds.bottom + workspaceBounds.height() / 16)
        item.drag(removeTarget, 600)
    }

    private companion object {
        const val MAX_DOCK_REMOVALS = 8
        const val MAX_WIDGET_REMOVAL_ATTEMPTS = 8
    }
}
