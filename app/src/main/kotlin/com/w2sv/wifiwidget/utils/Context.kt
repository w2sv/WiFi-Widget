package com.w2sv.wifiwidget.utils

import android.content.Context
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat

val Context.playStoreLink: String
    get() = "https://play.google.com/store/apps/details?id=$packageName"

val Context.locationServicesEnabled: Boolean
    get() = LocationManagerCompat.isLocationEnabled(getSystemService(LocationManager::class.java))