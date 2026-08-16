package com.w2sv.widget.ui.theme

import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider

@SuppressLint("RestrictedApi")
internal fun Int.toColorProvider(): ColorProvider =
    ColorProvider(Color(this))
