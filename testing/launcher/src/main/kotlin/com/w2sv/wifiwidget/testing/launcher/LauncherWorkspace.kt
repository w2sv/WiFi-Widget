package com.w2sv.wifiwidget.testing.launcher

import android.graphics.Point
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.uiautomator.By
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

/** Finds and removes launcher widgets; screenshot runs may also clear the dock. */
class LauncherWorkspace(private val launcher: LauncherDevice, private val formFactor: LauncherFormFactor) {
    private val instrumentation = launcher.instrumentation
    private val device = launcher.ui
    private val appPackage = launcher.appPackage

    fun prepare() {
        launcher.waitForLauncherToSettle(requireHotseat = formFactor == LauncherFormFactor.LargeTablet)
        clearDock()
        launcher.waitForLauncherToSettle(requireHotseat = formFactor == LauncherFormFactor.LargeTablet)
        checkDockEmpty()
    }

    private fun clearDock() {
        repeat(MAX_DOCK_REMOVALS) {
            val hotseat = checkNotNull(device.wait(Until.findObject(By.res(LAUNCHER_PACKAGE, "hotseat")), UI_TIMEOUT_MS)) {
                "Launcher3 hotseat is unavailable. ${launcher.hierarchy()}"
            }
            val shortcut = hotseat.findObjects(By.clazz("android.widget.TextView")).firstOrNull() ?: return
            val label = shortcut.text
            removeDockShortcut(shortcut)
            check(device.wait(Until.hasObject(By.res(LAUNCHER_PACKAGE, "label").text("Item removed")), UI_TIMEOUT_MS)) {
                "Launcher3 did not remove dock shortcut $label. ${launcher.hierarchy()}"
            }
            check(device.wait(Until.gone(By.res(LAUNCHER_PACKAGE, "label")), UI_TIMEOUT_MS)) {
                "Launcher3 removal message did not clear. ${launcher.hierarchy()}"
            }
        }
        checkDockEmpty()
    }

    fun clearExistingWidgets() {
        var removalAttempts = 0
        var lastStaleWidget: StaleObjectException? = null
        while (true) {
            val existingIds = launcher.widgetIds()
            if (existingIds.isEmpty()) break
            check(removalAttempts < MAX_WIDGET_REMOVAL_ATTEMPTS) {
                "Launcher3 still hosts WifiWidgetProvider IDs $existingIds after $MAX_WIDGET_REMOVAL_ATTEMPTS removal attempts. " +
                    "lastStaleWidget=$lastStaleWidget. ${launcher.hierarchy()}"
            }
            val widget = launcher.awaitValue("widget for installed IDs $existingIds") { launcher.findWidgetOnPages() }
            removalAttempts++
            try {
                removeWidget(widget)
            } catch (exception: StaleObjectException) {
                // The collection host can be replaced between discovery and the drag.
                lastStaleWidget = exception
                continue
            }
            if (!launcher.pollUntil { launcher.widgetIds() != existingIds }) {
                device.pressBack()
                launcher.waitForLauncherToSettle(requireHotseat = formFactor == LauncherFormFactor.LargeTablet)
                continue
            }
            launcher.await("Launcher3 widget removal message to clear") {
                !device.hasObject(By.res(LAUNCHER_PACKAGE, "label"))
            }
        }
        check(launcher.widgetIds().isEmpty()) {
            "WifiWidgetProvider IDs remain after launcher cleanup. ${launcher.hierarchy()}"
        }
        launcher.await("old widget view to disappear after its IDs were removed") {
            !device.hasObject(By.res(appPackage, "widget_layout"))
        }
    }

    fun checkDockEmpty() {
        val hotseat = checkNotNull(device.wait(Until.findObject(By.res(LAUNCHER_PACKAGE, "hotseat")), UI_TIMEOUT_MS)) {
            "Launcher3 hotseat is unavailable. ${launcher.hierarchy()}"
        }
        check(hotseat.findObjects(By.clazz("android.widget.TextView")).isEmpty()) {
            "Launcher3 dock contains shortcuts or suggestions. ${launcher.hierarchy()}"
        }
    }

    private fun removeDockShortcut(shortcut: UiObject2) {
        when (formFactor) {
            LauncherFormFactor.Phone -> dragPhoneItemToRemove(shortcut)
            LauncherFormFactor.Tablet,
            LauncherFormFactor.LargeTablet -> removeTabletDockShortcut(shortcut)
        }
    }

    private fun removeWidget(widget: UiObject2) {
        when (formFactor) {
            LauncherFormFactor.Phone -> dragPhoneItemToRemove(widget)
            LauncherFormFactor.Tablet,
            LauncherFormFactor.LargeTablet -> removeTabletWidget()
        }
    }

    private fun removeTabletDockShortcut(shortcut: UiObject2) {
        val label = shortcut.text
        val accessibilityNode = instrumentation.uiAutomation.rootInActiveWindow
            ?.findAccessibilityNodeInfosByText(label)
            ?.firstOrNull { it.text?.toString() == label }
        val moveAction = accessibilityNode?.actionList?.firstOrNull { it.label?.toString() == "Move item" }
        check(moveAction != null && accessibilityNode.performAction(moveAction.id)) {
            "Launcher3 did not start moving dock shortcut $label. ${launcher.hierarchy()}"
        }
        val removeTarget = checkNotNull(
            device.wait(Until.findObject(By.res(LAUNCHER_PACKAGE, "delete_target_text")), UI_TIMEOUT_MS)
        ) { "Launcher3 did not expose a Remove target. ${launcher.hierarchy()}" }
        removeTarget.click()
    }

    private fun removeTabletWidget() {
        val root = checkNotNull(instrumentation.uiAutomation.rootInActiveWindow) {
            "Launcher3 accessibility tree is unavailable. ${launcher.hierarchy()}"
        }
        val host = checkNotNull(findWidgetHost(root)) {
            "Launcher3 widget host is unavailable. ${launcher.hierarchy()}"
        }
        val moveAction = host.actionList.firstOrNull { it.label?.toString() == "Move item" }
        check(moveAction != null && host.performAction(moveAction.id)) {
            "Launcher3 widget host has no usable Move item action: ${host.actionList}. ${launcher.hierarchy()}"
        }
        val removeTarget = checkNotNull(
            device.wait(Until.findObject(By.res(LAUNCHER_PACKAGE, "delete_target_text")), UI_TIMEOUT_MS)
        ) { "Launcher3 did not expose a widget Remove target. ${launcher.hierarchy()}" }
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
            "Launcher3 workspace is unavailable. ${launcher.hierarchy()}"
        }.visibleBounds
        val statusBarBounds = checkNotNull(device.findObject(By.res(SYSTEM_UI_PACKAGE, "status_bar"))) {
            "System UI status bar is unavailable. ${launcher.hierarchy()}"
        }.visibleBounds
        val removeTarget = Point(workspaceBounds.centerX(), statusBarBounds.bottom + workspaceBounds.height() / 16)
        item.drag(removeTarget, 600)
    }

    private companion object {
        const val MAX_DOCK_REMOVALS = 8
        const val MAX_WIDGET_REMOVAL_ATTEMPTS = 8
    }
}
