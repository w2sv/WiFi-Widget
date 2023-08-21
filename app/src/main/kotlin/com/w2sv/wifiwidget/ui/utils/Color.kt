package com.w2sv.wifiwidget.ui.utils

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun @receiver:ColorInt Int.toColor(): Color =
    Color(this)

fun circularTrifoldStripeBrush(a: Color, b: Color, c: Color): Brush =
    Brush.linearGradient(
        0.4f to a,
        0.4f to b,
        0.6f to b,
        0.6f to c,
    )

fun circularTrifoldStripeBrush(colors: Triple<Color, Color, Color>): Brush =
    circularTrifoldStripeBrush(colors.first, colors.second, colors.third)
