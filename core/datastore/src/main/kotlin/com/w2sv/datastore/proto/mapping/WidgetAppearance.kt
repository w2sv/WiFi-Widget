package com.w2sv.datastore.proto.mapping

import com.w2sv.datastore.AlignmentProto
import com.w2sv.datastore.FontSizeProto
import com.w2sv.datastore.WidgetAppearanceProto
import com.w2sv.domain.model.widget.Alignment
import com.w2sv.domain.model.widget.FontSize
import com.w2sv.domain.model.widget.WidgetAppearance

internal fun WidgetAppearance.toProto(): WidgetAppearanceProto =
    WidgetAppearanceProto.newBuilder().also { builder ->
        builder.coloring = coloringConfig.toProto()
        builder.backgroundOpacity = backgroundOpacity
        builder.fontSize = fontSize.toProto()
        builder.propertyValueAlignment = propertyValueAlignment.toProto()
    }.build()

internal fun WidgetAppearanceProto.toExternal(): WidgetAppearance =
    WidgetAppearance(
        coloringConfig = coloring.toExternal(),
        backgroundOpacity = backgroundOpacity,
        fontSize = fontSize.toExternal(),
        propertyValueAlignment = propertyValueAlignment.toExternal()
    )

/* -------------------------
 * FontSize
 * ------------------------- */

private fun FontSize.toProto(): FontSizeProto =
    when (this) {
        FontSize.VerySmall -> FontSizeProto.VERY_SMALL
        FontSize.Small -> FontSizeProto.SMALL
        FontSize.Medium -> FontSizeProto.MEDIUM
        FontSize.Large -> FontSizeProto.LARGE
        FontSize.VeryLarge -> FontSizeProto.VERY_LARGE
    }

private fun FontSizeProto.toExternal(): FontSize =
    when (this) {
        FontSizeProto.VERY_SMALL -> FontSize.VerySmall
        FontSizeProto.SMALL -> FontSize.Small
        FontSizeProto.MEDIUM, FontSizeProto.UNRECOGNIZED -> FontSize.Medium
        FontSizeProto.LARGE -> FontSize.Large
        FontSizeProto.VERY_LARGE -> FontSize.VeryLarge
    }

/* -------------------------
 * Alignment
 * ------------------------- */

private fun Alignment.toProto(): AlignmentProto =
    when (this) {
        Alignment.Left -> AlignmentProto.LEFT
        Alignment.Right -> AlignmentProto.RIGHT
    }

private fun AlignmentProto.toExternal(): Alignment =
    when (this) {
        AlignmentProto.LEFT, AlignmentProto.UNRECOGNIZED -> Alignment.Left
        AlignmentProto.RIGHT -> Alignment.Right
    }
