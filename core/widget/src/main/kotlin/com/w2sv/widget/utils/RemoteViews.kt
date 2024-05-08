package com.w2sv.widget.utils

import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.IdRes

internal fun RemoteViews.setTextView(
    @IdRes viewId: Int,
    text: CharSequence,
    size: Float? = null,
    @ColorInt color: Int? = null
) {
    setViewVisibility(viewId, View.VISIBLE)
    setTextViewText(viewId, text)
    size?.let { setTextViewTextSize(viewId, COMPLEX_UNIT_SP, it) }
    color?.let { setTextColor(viewId, it) }
}
