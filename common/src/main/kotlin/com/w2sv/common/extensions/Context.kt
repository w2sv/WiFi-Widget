package com.w2sv.common.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.w2sv.androidutils.extensions.openUrl
import com.w2sv.androidutils.extensions.showToast

fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Context.openUrlWithActivityNotFoundHandling(url: String) {
    try {
        openUrl(url)
    } catch (e: ActivityNotFoundException) {
        showToast("Couldn't find any browser to open the URL with", Toast.LENGTH_LONG)
    }
}