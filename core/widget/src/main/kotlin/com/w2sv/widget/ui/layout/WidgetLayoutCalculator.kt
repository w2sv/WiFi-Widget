package com.w2sv.widget.ui.layout

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.layout.Alignment
import com.w2sv.domain.widget.WifiPropertyValueAlignment
import com.w2sv.widget.ui.state.FormattedRenderTime
import com.w2sv.widget.ui.state.WidgetUtilities
import com.w2sv.widget.ui.theme.WidgetDimens

internal fun calculateWidgetLayout(
    size: DpSize,
    utilities: WidgetUtilities,
    renderTime: FormattedRenderTime,
    valueAlignment: WifiPropertyValueAlignment
): WidgetLayout {
    val actions = calculateActions(size, utilities.actions.size)
    val timestamp = calculateTimestamp(size, utilities.showRefreshTime, renderTime, actions)
    val overlayHeight = maxOf(
        if (utilities.actions.isNotEmpty()) actions.buttonSize else 0.dp,
        if (timestamp != null) WidgetDimens.Timestamp.height else 0.dp
    )
    return WidgetLayout(
        actions = actions,
        timestamp = timestamp,
        status = calculateStatus(size, actions, timestamp != null),
        properties = calculateProperties(size, valueAlignment),
        bottomClearance = if (overlayHeight > 0.dp) overlayHeight + actions.edge + WidgetDimens.Actions.overlayGap else 0.dp
    )
}

private fun calculateActions(size: DpSize, count: Int): ActionLayout {
    val compact = size.height < WidgetDimens.Breakpoints.compactHeight || size.width < WidgetDimens.Breakpoints.compactWidth
    val tokens = WidgetDimens.Actions
    val edge = if (compact) tokens.compactEdge else tokens.regularEdge
    val spacing = if (compact) tokens.compactSpacing else tokens.regularSpacing
    val buttonSize = when {
        size.height <= WidgetDimens.Breakpoints.minimumHeight || size.width <= WidgetDimens.Breakpoints.minimumWidth -> tokens.minimumSize
        size.width >= WidgetDimens.Breakpoints.regularActionWidth &&
            size.height >= WidgetDimens.Breakpoints.regularActionHeight -> tokens.regularSize
        else -> tokens.compactSize
    }
    return ActionLayout(
        edge = edge,
        buttonSize = buttonSize,
        iconSize = if (buttonSize < tokens.compactSize) tokens.compactIcon else tokens.regularIcon,
        spacing = spacing,
        totalWidth = buttonSize * count + spacing * (count - 1).coerceAtLeast(0)
    )
}

private fun calculateTimestamp(
    size: DpSize,
    enabled: Boolean,
    renderTime: FormattedRenderTime,
    actions: ActionLayout
): TimestampLayout? {
    if (!enabled) return null
    val endClearance = actions.totalWidth + if (actions.totalWidth > 0.dp) actions.spacing else 0.dp
    val availableWidth = size.width - actions.edge * 2 - endClearance
    val minimumHeight = size.height <= WidgetDimens.Breakpoints.minimumHeight
    val tokens = WidgetDimens.Timestamp
    return TimestampLayout(
        text = if (!minimumHeight && availableWidth >= tokens.normalTextWidth) renderTime.normal else renderTime.compact,
        fontSize = if (minimumHeight) tokens.minimumHeightFontSize else tokens.fontSize,
        endClearance = endClearance,
        horizontalPadding = if (minimumHeight) tokens.minimumHeightHorizontalPadding else tokens.horizontalPadding,
        verticalPadding = if (minimumHeight) tokens.minimumHeightVerticalPadding else tokens.verticalPadding
    )
}

private fun calculateStatus(
    size: DpSize,
    actions: ActionLayout,
    timestampEnabled: Boolean
): StatusLayout {
    val minimumHeight = size.height <= WidgetDimens.Breakpoints.minimumHeight
    val tokens = WidgetDimens.Status
    val presentation = when {
        size.width <= WidgetDimens.Breakpoints.minimumWidth -> StatusPresentation.TextOnly
        minimumHeight -> StatusPresentation.Horizontal
        else -> StatusPresentation.Vertical
    }
    val minimumSize = minimumHeight && presentation == StatusPresentation.TextOnly
    val endPadding = if (minimumHeight && !minimumSize && actions.totalWidth > 0.dp) actions.totalWidth + actions.edge else 0.dp
    val startPadding = if (minimumHeight && !minimumSize && timestampEnabled) {
        minOf(actions.edge + WidgetDimens.Timestamp.minimumHeightWidth, (size.width - endPadding).coerceAtLeast(0.dp))
    } else {
        0.dp
    }
    return StatusLayout(
        presentation = presentation,
        iconSize = if (presentation == StatusPresentation.Vertical &&
            size.height >= WidgetDimens.Breakpoints.regularActionHeight
        ) {
            tokens.regularIcon
        } else {
            tokens.compactIcon
        },
        spacing = if (presentation == StatusPresentation.Vertical) tokens.verticalGap else tokens.horizontalGap,
        startPadding = startPadding,
        endPadding = endPadding,
        bottomPadding = if (!minimumHeight && (actions.totalWidth > 0.dp || timestampEnabled)) tokens.overlayPadding else 0.dp,
        contentAlignment = if (minimumSize) Alignment.TopCenter else Alignment.Center
    )
}

private fun calculateProperties(size: DpSize, alignment: WifiPropertyValueAlignment): PropertyLayout {
    val tokens = WidgetDimens.Properties
    val alignedWidth = minOf(tokens.maxLabelWidth, size.width * tokens.alignedLabelFraction)
    val labelWidth = when {
        alignment == WifiPropertyValueAlignment.Left -> alignedWidth
        size.width < WidgetDimens.Breakpoints.compactLabels -> size.width * tokens.compactLabelFraction
        else -> null
    }
    return PropertyLayout(labelWidth, if (alignment == WifiPropertyValueAlignment.Left) alignedWidth else 0.dp)
}
