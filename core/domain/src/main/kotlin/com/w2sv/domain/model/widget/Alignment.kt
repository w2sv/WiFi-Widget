package com.w2sv.domain.model.widget

import androidx.annotation.StringRes
import com.w2sv.core.domain.R
import com.w2sv.domain.model.Labelled

enum class Alignment(@StringRes override val labelRes: Int) : Labelled {
    Left(R.string.left),
    Right(R.string.right)
}
