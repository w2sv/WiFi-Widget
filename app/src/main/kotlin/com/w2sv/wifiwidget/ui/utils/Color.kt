package com.w2sv.wifiwidget.ui.utils

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun @receiver:ColorInt Int.toColor(): Color =
    Color(this)

fun circularTrifoldStripeBrush(colors: Triple<Color, Color, Color>): Brush =
    Brush.linearGradient(
        0.4f to colors.first,
        0.4f to colors.second,
        0.6f to colors.second,
        0.6f to colors.third
    )