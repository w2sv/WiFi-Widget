package com.w2sv.wifiwidget.widget.utils

import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IdRes

internal fun RemoteViews.crossVisualize(@IdRes showView: Int, @IdRes hideView: Int) {
    setViewVisibility(showView, View.VISIBLE)
    setViewVisibility(hideView, View.GONE)
}