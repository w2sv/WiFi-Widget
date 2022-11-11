package com.w2sv.wifiwidget.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.IdRes
import androidx.core.content.edit
import com.w2sv.wifiwidget.R

object AppPreferences {
    fun instance(context: Context): SharedPreferences =
        context.run {
            getSharedPreferences(packageName, Context.MODE_PRIVATE)
        }

    var SharedPreferences.showSSID: Boolean
        get() = getBoolean("showSSID", true)
        set(value) {
            edit { putBoolean("showSSID", value) }
        }

    var SharedPreferences.locationPermissionDialogShown: Boolean
        get() = getBoolean("showedLocationPermissionRational", false)
        set(value) {
            edit { putBoolean("showedLocationPermissionRational", value) }
        }

    fun SharedPreferences.booleanPreferenceFromResourceId(@IdRes id: Int): Boolean? =
        mapOf(
            R.id.ssid_tv to { showSSID }
        )[id]
            ?.invoke()
}

fun Context.appPreferences(): SharedPreferences =
    AppPreferences.instance(this)