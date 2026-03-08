package com.w2sv.domain.model.wifiproperty.viewdata

import android.text.SpannedString
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.core.text.subscript

data class SubscriptableText(val text: String, val subscript: String? = null) {
    fun toSpannedString(subscriptScale: Float): SpannedString =
        buildSpannedString {
            append(text)
            subscript?.let { subscript { scale(subscriptScale) { append(it) } } }
        }
}
