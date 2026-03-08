package com.w2sv.domain.model.widget

import androidx.annotation.ColorInt
import com.w2sv.androidutils.graphics.getAlphaSetColor

data class WidgetColors(@ColorInt val background: Int, @ColorInt val primary: Int, @ColorInt val secondary: Int) {
    val subPropertyBackground by lazy {
        getAlphaSetColor(primary, 0.25f)
    }
}
