package com.w2sv.widget.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.glance.unit.ColorProvider
import com.w2sv.domain.widget.WidgetColors

internal fun Int.toColorProvider(): ColorProvider =
    ColorProvider(Color(this))

internal val WidgetColors.subPropertyText: Int
    get() = ColorUtils.blendARGB(secondary, background, 0.2f)
