package com.w2sv.wifiwidget.testing.launcher

import android.graphics.Rect
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
        launcher.showLauncher()
        leaveTransientLauncherMode()
        var removedWidgets = 0
        var staleAttempts = 0
        var lastStaleWidget: StaleObjectException? = null
        while (true) {
            val existingIds = launcher.widgetIds()
            if (existingIds.isEmpty()) break
            check(removedWidgets < MAX_WIDGET_REMOVALS) {
                "Launcher3 still hosts WifiWidgetProvider IDs $existingIds after removing $removedWidgets widgets; " +
                    "lastStaleWidget=$lastStaleWidget. ${removalDiagnostics()}"
            }
            val widget = launcher.awaitValue("widget for installed IDs $existingIds") { launcher.findWidgetOnPages() }
            try {
                removeWidget(widget)
            } catch (exception: StaleObjectException) {
                // The collection host can be replaced between discovery and the accessibility action.
                lastStaleWidget = exception
                staleAttempts++
                val diagnostics = removalDiagnostics()
                leaveTransientLauncherMode()
                check(staleAttempts <= MAX_STALE_WIDGET_RETRIES) {
                    "Launcher3 widget host became stale $staleAttempts times for IDs $existingIds; " +
                        "lastStaleWidget=$lastStaleWidget. $diagnostics"
                }
                continue
            } catch (failure: Throwable) {
                val diagnostics = removalDiagnostics()
                runCatching { leaveTransientLauncherMode() }.onFailure(failure::addSuppressed)
                throw AssertionError("Launcher3 could not remove widget for IDs $existingIds. $diagnostics", failure)
            }
            if (!launcher.pollUntil(WIDGET_REMOVAL_TIMEOUT_MS) { (existingIds - launcher.widgetIds()).isNotEmpty() }) {
                val diagnostics = removalDiagnostics()
                val failure = AssertionError("Launcher3 Remove left WifiWidgetProvider IDs $existingIds unchanged. $diagnostics")
                runCatching { leaveTransientLauncherMode() }.onFailure(failure::addSuppressed)
                throw failure
            }
            removedWidgets += (existingIds - launcher.widgetIds()).size
            staleAttempts = 0
            leaveTransientLauncherMode()
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
        val label = shortcut.text
        val accessibilityNode = instrumentation.uiAutomation.rootInActiveWindow
            ?.findAccessibilityNodeInfosByText(label)
            ?.firstOrNull { it.text?.toString() == label }
        checkNotNull(accessibilityNode) {
            "Launcher3 dock shortcut $label is missing from the accessibility tree. ${launcher.hierarchy()}"
        }
        performMoveAction(accessibilityNode, "dock shortcut $label")
        activateRemoveTarget()
    }

    private fun removeWidget(widget: UiObject2) {
        val bounds = widget.visibleBounds
        val root = checkNotNull(instrumentation.uiAutomation.rootInActiveWindow) {
            "Launcher3 accessibility tree is unavailable. ${removalDiagnostics()}"
        }
        val host = checkNotNull(findWidgetHost(root, bounds)) {
            "Launcher3 widget host is unavailable at $bounds. ${removalDiagnostics()}"
        }
        performMoveAction(host, "widget host at $bounds")
        activateRemoveTarget()
    }

    private fun performMoveAction(node: AccessibilityNodeInfo, description: String) {
        val moveAction = node.actionList.firstOrNull { it.label?.toString() == "Move item" }
        check(moveAction != null && node.performAction(moveAction.id)) {
            "Launcher3 did not start moving $description; actions=${node.actionList}. ${removalDiagnostics()}"
        }
    }

    private fun activateRemoveTarget() {
        val removeTarget = checkNotNull(
            device.wait(Until.findObject(By.res(LAUNCHER_PACKAGE, "delete_target_text")), REMOVE_TARGET_TIMEOUT_MS)
        ) { "Launcher3 did not expose a Remove target. ${removalDiagnostics()}" }
        val clickedWithAccessibility = instrumentation.uiAutomation.rootInActiveWindow
            ?.findAccessibilityNodeInfosByViewId("$LAUNCHER_PACKAGE:id/delete_target_text")
            ?.firstOrNull()
            ?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
        if (!clickedWithAccessibility) removeTarget.click()
    }

    private fun findWidgetHost(node: AccessibilityNodeInfo, widgetBounds: Rect): AccessibilityNodeInfo? {
        if (node.className?.toString() == WIDGET_HOST_CLASS) {
            val hostBounds = Rect()
            node.getBoundsInScreen(hostBounds)
            if (hostBounds.contains(widgetBounds.centerX(), widgetBounds.centerY())) return node
        }
        for (index in 0 until node.childCount) {
            node.getChild(index)?.let { child -> findWidgetHost(child, widgetBounds)?.let { return it } }
        }
        return null
    }

    private fun removalDiagnostics(): String {
        val ids = runCatching { launcher.widgetIds() }.getOrElse { "unavailable: $it" }
        val accessibility = runCatching {
            val root = instrumentation.uiAutomation.rootInActiveWindow
            val hosts = mutableListOf<AccessibilityNodeInfo>()
            if (root != null) collectWidgetHosts(root, hosts)
            val hostActions = hosts.map { host ->
                val bounds = Rect()
                host.getBoundsInScreen(bounds)
                "$bounds actions=${host.actionList}"
            }
            val targets = root?.findAccessibilityNodeInfosByViewId("$LAUNCHER_PACKAGE:id/delete_target_text")
                .orEmpty()
                .map { target ->
                    val bounds = Rect()
                    target.getBoundsInScreen(bounds)
                    "$bounds actions=${target.actionList}"
                }
            "widgetHosts=$hostActions, removeTargets=$targets"
        }.getOrElse { "accessibility unavailable: $it" }
        val hierarchy = runCatching { launcher.hierarchy() }.getOrElse { "unavailable: $it" }
        return "widgetIds=$ids, $accessibility, hierarchy=$hierarchy"
    }

    private fun collectWidgetHosts(node: AccessibilityNodeInfo, hosts: MutableList<AccessibilityNodeInfo>) {
        if (node.className?.toString() == WIDGET_HOST_CLASS) hosts.add(node)
        for (index in 0 until node.childCount) {
            node.getChild(index)?.let { collectWidgetHosts(it, hosts) }
        }
    }

    private fun leaveTransientLauncherMode() {
        val dragTarget = By.res(LAUNCHER_PACKAGE, "delete_target_text")
        val resizeFrame = By.res(LAUNCHER_PACKAGE, "widget_resize_frame")
        repeat(MAX_TRANSIENT_MODE_EXITS) {
            if (!device.hasObject(dragTarget) && !device.hasObject(resizeFrame)) return
            device.pressBack()
            if (device.wait(Until.gone(dragTarget), REMOVE_TARGET_TIMEOUT_MS) &&
                device.wait(Until.gone(resizeFrame), REMOVE_TARGET_TIMEOUT_MS)
            ) {
                return
            }
        }
        check(!device.hasObject(dragTarget) && !device.hasObject(resizeFrame)) {
            "Launcher3 remained in drag or resize mode. ${removalDiagnostics()}"
        }
    }

    private companion object {
        const val MAX_DOCK_REMOVALS = 8
        const val MAX_WIDGET_REMOVALS = 8
        const val MAX_STALE_WIDGET_RETRIES = 2
        const val MAX_TRANSIENT_MODE_EXITS = 2
        const val WIDGET_REMOVAL_TIMEOUT_MS = 5_000L
        const val REMOVE_TARGET_TIMEOUT_MS = 5_000L
        const val WIDGET_HOST_CLASS = "com.android.launcher3.widget.LauncherAppWidgetHostView"
    }
}
