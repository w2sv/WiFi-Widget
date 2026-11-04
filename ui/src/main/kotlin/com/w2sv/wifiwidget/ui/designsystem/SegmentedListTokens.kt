package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object SegmentedListTokens {
    val itemSpacing = ListItemDefaults.SegmentedGap
    val defaultElevation = 1.dp

    @Composable
    fun colors(): ListItemColors =
        ListItemDefaults.segmentedColors(containerColor = CardDefaults.elevatedCardColors().containerColor)
}
