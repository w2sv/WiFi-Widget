package com.w2sv.widget.ui.theme

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.VisibleForTesting
import androidx.core.graphics.ColorUtils
import com.w2sv.androidutils.graphics.getAlphaSetColor
import com.w2sv.domain.widget.WidgetAppearance
import com.w2sv.domain.widget.WidgetColors
import com.w2sv.domain.widget.WidgetConfig

/** Final ARGB colors for one widget render, including background opacity and derived colors. */
internal data class WidgetRenderColors(
    @ColorInt val background: Int,
    @ColorInt val primary: Int,
    @ColorInt val secondary: Int,
    @ColorInt val floatingSurface: Int,
    @ColorInt val subPropertyText: Int
)

internal fun WidgetAppearance.renderColors(context: Context): WidgetRenderColors =
    coloring.resolve(context).toRenderColors(backgroundOpacity)

@VisibleForTesting
internal fun WidgetColors.toRenderColors(backgroundOpacity: Float): WidgetRenderColors {
    val resolvedBackground = getAlphaSetColor(background, backgroundOpacity)
    return WidgetRenderColors(
        background = resolvedBackground,
        primary = primary,
        secondary = secondary,
        floatingSurface = ColorUtils.setAlphaComponent(
            ColorUtils.blendARGB(background or -0x1000000, primary or -0x1000000, 0.12f),
            Color.alpha(resolvedBackground)
        ),
        subPropertyText = ColorUtils.blendARGB(secondary, background, 0.2f)
    )
}
