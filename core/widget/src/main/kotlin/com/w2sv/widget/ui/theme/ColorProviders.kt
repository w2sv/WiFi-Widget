package com.w2sv.widget.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider

internal fun Int.toColorProvider(): ColorProvider =
    ColorProvider(Color(this))
