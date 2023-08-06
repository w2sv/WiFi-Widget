package com.w2sv.widget.utils

import android.view.View
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.IdRes

fun RemoteViews.setTextView(@IdRes viewId: Int, text: String, @ColorInt color: Int?) {
    setViewVisibility(viewId, View.VISIBLE)
    setTextViewText(viewId, text)
    color?.let {
        setTextColor(viewId, it)
    }
}