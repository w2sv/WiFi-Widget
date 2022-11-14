package com.w2sv.wifiwidget.preferences

import androidx.annotation.IdRes
import com.w2sv.typedpreferences.descendants.BooleanPreferences
import com.w2sv.wifiwidget.R

object BooleanPreferences: BooleanPreferences("showSSID" to true, "locationPermissionDialogShown" to false) {

        var showSSID by this
        var locationPermissionDialogShown by this

        fun fromResourceId(@IdRes id: Int): Boolean? =
            mapOf(
                R.id.ssid_tv to showSSID
            )[id]
    }
