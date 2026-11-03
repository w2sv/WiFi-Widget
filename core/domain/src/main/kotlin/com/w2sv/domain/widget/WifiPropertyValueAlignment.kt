package com.w2sv.domain.widget

import androidx.annotation.StringRes
import com.w2sv.core.resources.R
import com.w2sv.domain.WithLabel

enum class WifiPropertyValueAlignment(@StringRes override val labelRes: Int) : WithLabel {
    Left(R.string.left),
    Right(R.string.right)
}
