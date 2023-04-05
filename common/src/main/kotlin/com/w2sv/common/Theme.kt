package com.w2sv.common

import androidx.annotation.ColorInt
import androidx.annotation.ColorRes

enum class Theme {
    Light,
    DeviceDefault,
    Dark,
    Custom
}

sealed class ColorPalette {
    class Light : ColorPalette() {
        @ColorRes
        val backgroundRes: Int = android.R.color.background_light
        @ColorRes
        val labelsRes: Int = R.color.blue_chill
        @ColorRes
        val otherRes: Int = androidx.appcompat.R.color.foreground_material_dark
    }

    object DeviceDefault : ColorPalette()

    class Dark : ColorPalette() {
        @ColorRes
        val backgroundRes: Int = android.R.color.background_dark
        @ColorRes
        val labelsRes: Int = R.color.blue_chill
        @ColorRes
        val otherRes: Int = androidx.appcompat.R.color.foreground_material_light
    }

    class Custom(
        @ColorInt val background: Int,
        @ColorInt val labels: Int,
        @ColorInt val other: Int
    ) : ColorPalette()
}