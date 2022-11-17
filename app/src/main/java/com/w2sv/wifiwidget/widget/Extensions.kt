package com.w2sv.wifiwidget.widget

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.format.Formatter
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IdRes

internal fun RemoteViews.crossVisualize(@IdRes showView: Int, @IdRes hideView: Int) {
    setViewVisibility(showView, View.VISIBLE)
    setViewVisibility(hideView, View.GONE)
}

@Suppress("DEPRECATION")
internal fun Int.asFormattedIpAddress(): String =
    Formatter.formatIpAddress(this)

internal val ConnectivityManager.isWifiConnected: Boolean
    get() =
        getNetworkCapabilities(activeNetwork)
            ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true