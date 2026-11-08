package com.w2sv.widget.ui.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.glance.layout.Alignment

internal data class WidgetLayout(
    val actions: ActionLayout,
    val timestamp: TimestampLayout?,
    val status: StatusLayout,
    val properties: PropertyLayout,
    val bottomClearance: Dp
)

internal data class ActionLayout(val edge: Dp, val buttonSize: Dp, val iconSize: Dp, val spacing: Dp, val totalWidth: Dp)

internal data class TimestampLayout(
    val text: String,
    val fontSize: TextUnit,
    val endClearance: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp
)

internal enum class StatusPresentation { Vertical, Horizontal, TextOnly }

internal data class StatusLayout(
    val presentation: StatusPresentation,
    val iconSize: Dp,
    val spacing: Dp,
    val startPadding: Dp,
    val endPadding: Dp,
    val bottomPadding: Dp,
    val contentAlignment: Alignment
)

internal data class PropertyLayout(val labelColumnWidth: Dp?, val subpropertyIndent: Dp)
