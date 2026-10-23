package com.w2sv.wifiwidget.storescreenshots.device

import com.w2sv.wifiwidget.testing.launcher.LauncherFormFactor
import com.w2sv.wifiwidget.testing.launcher.LauncherSizing

typealias GridCells = com.w2sv.wifiwidget.testing.launcher.GridCells

enum class ScreenshotDeviceCategory(
    val screenshotDirectory: String,
    val launcherGrid: GridCells,
    val initialWidgetSize: GridCells,
    val resizeTolerancePx: Int
) {
    Phone(
        "phone-screenshots",
        GridCells(5, 5),
        GridCells(3, 2),
        24
    ),
    Tablet(
        "tablet-screenshots",
        GridCells(6, 5),
        GridCells(2, 2),
        60
    ),
    LargeTablet(
        "large-tablet-screenshots",
        GridCells(6, 5),
        GridCells(2, 1),
        60
    );

    val launcherSizing: LauncherSizing
        get() = LauncherSizing(
            formFactor = when (this) {
                Phone -> LauncherFormFactor.Phone
                Tablet -> LauncherFormFactor.Tablet
                LargeTablet -> LauncherFormFactor.LargeTablet
            },
            grid = launcherGrid,
            initialWidgetSize = initialWidgetSize,
            resizeTolerancePx = resizeTolerancePx
        )
}
