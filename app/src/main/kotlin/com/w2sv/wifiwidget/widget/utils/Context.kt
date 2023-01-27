package com.w2sv.wifiwidget.widget.utils

import android.content.Context
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.wifiwidget.R

fun Context.showPinnedWidgetToast(){
    showToast(getString(R.string.pinned_widget))
}