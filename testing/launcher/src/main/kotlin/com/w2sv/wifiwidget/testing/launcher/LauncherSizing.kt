package com.w2sv.wifiwidget.testing.launcher

data class GridCells(val columns: Int, val rows: Int)

enum class LauncherFormFactor { Phone, Tablet, LargeTablet }

data class LauncherSizing(
    val formFactor: LauncherFormFactor,
    val grid: GridCells,
    val initialWidgetSize: GridCells,
    val resizeTolerancePx: Int
)
