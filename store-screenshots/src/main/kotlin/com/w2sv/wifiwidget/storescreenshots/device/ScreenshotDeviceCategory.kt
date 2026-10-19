package com.w2sv.wifiwidget.storescreenshots.device

data class GridCells(val columns: Int, val rows: Int)

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
    )
}
