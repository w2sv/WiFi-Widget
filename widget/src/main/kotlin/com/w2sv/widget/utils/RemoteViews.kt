package com.w2sv.widget.utils

import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.IdRes

fun RemoteViews.setTextView(@IdRes viewId: Int, text: String, @ColorInt color: Int) {
    setTextViewText(viewId, text)
    setTextColor(viewId, color)
}