package com.w2sv.wifiwidget.testing.launcher

import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.os.SystemClock
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import kotlin.math.abs

/** Pins and resizes the real widget through the app's pinning entry point and Launcher3. */
class LauncherWidgetController(private val launcher: LauncherDevice, private val sizing: LauncherSizing) {
    private val instrumentation = launcher.instrumentation
    private val device = launcher.ui
    private val appPackage = launcher.appPackage

    fun pin(): Int {
        check(launcher.widgetManager.isRequestPinAppWidgetSupported) {
            "Launcher3 does not support requestPinAppWidget on this managed device."
        }
        val widgetIdsBeforePin = launcher.widgetIds()

        instrumentation.context.startActivity(
            Intent().setClassName(appPackage, "com.w2sv.wifiwidget.ui.WidgetPinActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )

        val confirmationButton = By.pkg(LAUNCHER_PACKAGE).text("ADD TO HOME SCREEN")

        check(device.wait(Until.hasObject(confirmationButton), UI_TIMEOUT_MS)) {
            "Launcher3 did not show the widget pin confirmation button.".withHierarchy()
        }
        device.findObject(confirmationButton).click()

        val newWidgetId = launcher.awaitValue("new widget ID after pin confirmation") {
            launcher.widgetIds().firstOrNull { it !in widgetIdsBeforePin }
        }
        check(device.wait(Until.gone(confirmationButton), UI_TIMEOUT_MS)) {
            "Launcher3 pin confirmation did not close.".withHierarchy()
        }
        check(device.wait(Until.hasObject(By.res(appPackage, "widget_layout")), UI_TIMEOUT_MS)) {
            "Pinned widget $newWidgetId is not visible on the current launcher page.".withHierarchy()
        }
        check(launcher.widgetIds() == setOf(newWidgetId)) {
            "The launcher hosts a different WifiWidgetProvider instance than the newly pinned ID $newWidgetId.".withHierarchy()
        }
        return newWidgetId
    }

    fun resize(targetSize: GridCells) {
        val initialSize = sizing.initialWidgetSize
        require(targetSize.columns in 1..sizing.grid.columns)
        require(targetSize.rows in 1..sizing.grid.rows)

        val widget = checkNotNull(device.findObject(By.res(appPackage, "widget_layout"))) {
            "The newly pinned widget is unavailable for resizing.".withHierarchy()
        }
        val original = Rect(widget.visibleBounds)
        val workspaceObject = checkNotNull(device.findObject(By.res(LAUNCHER_PACKAGE, "workspace"))) {
            "Launcher3 workspace is unavailable for resizing.".withHierarchy()
        }
        val metrics = when (sizing.formFactor) {
            LauncherFormFactor.Phone -> phoneResizeMetrics(original, workspaceObject)
            LauncherFormFactor.Tablet -> tabletResizeMetrics(original, workspaceObject)
            LauncherFormFactor.LargeTablet -> largeTabletResizeMetrics(workspaceObject)
        }
        // Launcher3 snaps each handle to its grid; this travel estimate is checked against the resulting bounds.
        val expectedWidth = original.width() + (targetSize.columns - initialSize.columns) * metrics.horizontalPitch
        val expectedHeight = original.height() + (targetSize.rows - initialSize.rows) * metrics.verticalPitch
        val widthChanged = targetSize.columns != initialSize.columns
        val heightChanged = targetSize.rows != initialSize.rows

        widget.longClick()
        check(device.wait(Until.hasObject(By.res(LAUNCHER_PACKAGE, "widget_resize_frame")), UI_TIMEOUT_MS)) {
            "Launcher3 did not enter widget resize mode.".withHierarchy()
        }
        if (widthChanged) {
            val handle = checkNotNull(device.findObject(By.res(LAUNCHER_PACKAGE, "widget_resize_right_handle"))) {
                "Launcher3's right resize handle is inaccessible.".withHierarchy()
            }
            val bounds = handle.visibleBounds
            val delta = (targetSize.columns - initialSize.columns) * metrics.horizontalPitch
            handle.drag(Point(bounds.centerX() + delta, bounds.centerY()), RESIZE_DRAG_SPEED)
        }
        if (heightChanged) {
            val handle = checkNotNull(device.findObject(By.res(LAUNCHER_PACKAGE, "widget_resize_bottom_handle"))) {
                "Launcher3's bottom resize handle is inaccessible.".withHierarchy()
            }
            val bounds = handle.visibleBounds
            val delta = (targetSize.rows - initialSize.rows) * metrics.verticalPitch
            handle.drag(Point(bounds.centerX(), bounds.centerY() + delta), RESIZE_DRAG_SPEED)
        }

        check(waitForWidgetBounds(original, expectedWidth, expectedHeight, widthChanged, heightChanged)) {
            (
                "Launcher3 did not resize the widget to ${targetSize.columns}x${targetSize.rows} cells; before=$original, " +
                    "after=${device.findObject(By.res(appPackage, "widget_layout"))?.visibleBounds}."
                ).withHierarchy()
        }
        device.pressBack()
        check(device.wait(Until.gone(By.res(LAUNCHER_PACKAGE, "widget_resize_frame")), UI_TIMEOUT_MS)) {
            "Launcher3 did not dismiss widget resize mode.".withHierarchy()
        }
        launcher.waitForLauncherToSettle(requireHotseat = sizing.formFactor == LauncherFormFactor.LargeTablet)
        check(device.wait(Until.gone(By.res(appPackage, "loading_tv")), UI_TIMEOUT_MS)) {
            "The resized widget is still loading.".withHierarchy()
        }
        check(waitForWidgetBounds(original, expectedWidth, expectedHeight, widthChanged, heightChanged)) {
            "The widget bounds changed after leaving resize mode.".withHierarchy()
        }
    }

    private fun phoneResizeMetrics(original: Rect, workspace: UiObject2): ResizeMetrics {
        val workspaceBounds = workspace.visibleBounds
        val sideMargin = original.left - workspaceBounds.left
        return ResizeMetrics(
            horizontalPitch = (workspaceBounds.width() - 2 * sideMargin - original.width()) /
                (sizing.grid.columns - sizing.initialWidgetSize.columns),
            verticalPitch = original.height() / sizing.initialWidgetSize.rows
        )
    }

    private fun tabletResizeMetrics(original: Rect, workspace: UiObject2): ResizeMetrics =
        ResizeMetrics(
            horizontalPitch = cellLayoutBounds(workspace).width() / sizing.grid.columns,
            verticalPitch = original.height() / sizing.initialWidgetSize.rows
        )

    private fun largeTabletResizeMetrics(workspace: UiObject2): ResizeMetrics {
        val cellLayout = cellLayoutBounds(workspace)
        return ResizeMetrics(
            horizontalPitch = cellLayout.width() / sizing.grid.columns,
            verticalPitch = cellLayout.height() / sizing.grid.rows
        )
    }

    private fun cellLayoutBounds(workspace: UiObject2): Rect =
        checkNotNull(workspace.children.firstOrNull()) {
            "Launcher3 workspace has no cell layout.".withHierarchy()
        }.visibleBounds

    private fun waitForWidgetBounds(
        original: Rect,
        expectedWidth: Int,
        expectedHeight: Int,
        widthRequested: Boolean,
        heightRequested: Boolean
    ): Boolean {
        val gridSnapTolerance = sizing.resizeTolerancePx
        val deadline = SystemClock.elapsedRealtime() + UI_TIMEOUT_MS
        while (SystemClock.elapsedRealtime() < deadline) {
            val current = device.findObject(By.res(appPackage, "widget_layout"))?.visibleBounds
            if (current != null &&
                abs(current.left - original.left) <= UNCHANGED_BOUNDS_TOLERANCE_PX &&
                abs(current.top - original.top) <= UNCHANGED_BOUNDS_TOLERANCE_PX &&
                abs(current.width() - expectedWidth) <=
                (if (widthRequested) gridSnapTolerance else UNCHANGED_BOUNDS_TOLERANCE_PX) &&
                abs(current.height() - expectedHeight) <=
                (if (heightRequested) gridSnapTolerance else UNCHANGED_BOUNDS_TOLERANCE_PX)
            ) {
                return true
            }
            SystemClock.sleep(POLL_INTERVAL_MS)
        }
        return false
    }

    private fun String.withHierarchy(): String =
        "$this ${launcher.hierarchy()}"

    private companion object {
        const val RESIZE_DRAG_SPEED = 600
        const val UNCHANGED_BOUNDS_TOLERANCE_PX = 2
    }

    private data class ResizeMetrics(val horizontalPitch: Int, val verticalPitch: Int)
}
