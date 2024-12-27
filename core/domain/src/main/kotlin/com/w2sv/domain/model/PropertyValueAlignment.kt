package com.w2sv.domain.model

import androidx.annotation.StringRes
import com.w2sv.core.domain.R

enum class PropertyValueAlignment(@StringRes override val labelRes: Int) : WidgetProperty {
    Left(R.string.left),
    Right(R.string.right)
}
