package com.w2sv.common.utils

import android.location.LocationManager
import android.os.Build

val LocationManager.isLocationEnabledCompat: Boolean?
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) isLocationEnabled else null