package com.w2sv.wifiwidget.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.widget.Toast
import com.w2sv.androidutils.extensions.openUrl
import com.w2sv.androidutils.extensions.showToast

fun Context.openUrlWithActivityNotFoundHandling(url: String) {
    try {
        openUrl(url)
    } catch (e: ActivityNotFoundException) {
        showToast("Couldn't find any browser to open the URL with", Toast.LENGTH_LONG)
    }
}