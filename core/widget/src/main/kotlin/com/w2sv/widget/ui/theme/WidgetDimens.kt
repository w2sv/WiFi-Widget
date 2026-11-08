package com.w2sv.widget.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object WidgetDimens {
    object Breakpoints {
        val minimumWidth = 100.dp
        val minimumHeight = 40.dp
        val compactWidth = 160.dp
        val compactHeight = 80.dp
        val regularActionWidth = 210.dp
        val regularActionHeight = 120.dp
        val compactLabels = 180.dp
    }

    object Actions {
        val compactEdge = 4.dp
        val regularEdge = 8.dp
        val compactSpacing = 4.dp
        val regularSpacing = 8.dp
        val minimumSize = 28.dp
        val compactSize = 40.dp
        val regularSize = 44.dp
        val compactIcon = 18.dp
        val regularIcon = 24.dp
        val overlayGap = 4.dp
    }

    object Timestamp {
        val fontSize = 11.sp
        val minimumHeightFontSize = 9.sp
        val height = 24.dp
        val normalTextWidth = 110.dp
        val minimumHeightWidth = 48.dp
        val cornerRadius = 12.dp
        val horizontalPadding = 7.dp
        val verticalPadding = 3.dp
        val minimumHeightHorizontalPadding = 4.dp
        val minimumHeightVerticalPadding = 2.dp
    }

    object Status {
        val compactIcon = 24.dp
        val regularIcon = 32.dp
        val horizontalGap = 6.dp
        val verticalGap = 6.dp
        val overlayPadding = 32.dp
    }

    object Properties {
        val listStartPadding = 8.dp
        val listEndPadding = 4.dp
        val listContentTopPadding = 5.dp
        val rowVerticalPadding = 2.dp
        val scrollbarClearance = 6.dp
        val labelTrailingPadding = 6.dp
        val maxLabelWidth = 100.dp
        val alignedLabelFraction = 0.45f
        val compactLabelFraction = 0.42f
    }
}
