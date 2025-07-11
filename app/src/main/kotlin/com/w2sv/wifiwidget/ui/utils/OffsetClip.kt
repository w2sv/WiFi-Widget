package com.w2sv.wifiwidget.ui.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlin.math.abs

/**
 * Shifts your child by [dy] (down if +, up if −) and
 * clips away the overflowing |dy| at the opposite edge.
 */
fun Modifier.offsetClip(dy: Dp): Modifier =
    then(OffsetClipModifier(dy))

/**
 * Offsets the contents vertically by [dy]:
 *  - positive [dy] → content shifts *down* and bottom is clipped
 *  - negative [dy] → content shifts *up* and top is clipped
 *
 * The layout height is reduced by |dy| so nothing overflows.
 */
private data class OffsetClipModifier(val dy: Dp) : LayoutModifier {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        val offsetPx = dy.roundToPx()
        // shrink height by the abs offset
        val clippedHeight = (placeable.height - abs(offsetPx)).coerceAtLeast(0)
        return layout(placeable.width, clippedHeight) {
            // positive offsetPx → place down; negative → place up
            placeable.place(0, offsetPx)
        }
    }
}
