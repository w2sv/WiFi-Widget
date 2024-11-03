package com.w2sv.widget.model

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import com.w2sv.androidutils.graphics.getAlphaSetColor

internal data class WidgetColors(
    @ColorInt val background: Int,
    @ColorInt val primary: Int,
    @ColorInt val secondary: Int
) {
    data class Resources(
        @ColorRes val background: Int,
        @ColorRes val primary: Int,
        @ColorRes val secondary: Int
    ) {
        fun getColors(context: Context): WidgetColors =
            WidgetColors(
                context.getColor(background),
                context.getColor(primary),
                context.getColor(secondary)
            )
    }

    val ipSubPropertyBackgroundColor by lazy {
        getAlphaSetColor(primary, 0.25f)
    }
}
