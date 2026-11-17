package com.w2sv.wifiwidget.ui.util

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
internal val requiresBackgroundLocationPermission: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
internal val backgroundLocationRequiresSettings: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
