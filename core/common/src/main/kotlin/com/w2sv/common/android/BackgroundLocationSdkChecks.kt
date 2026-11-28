package com.w2sv.common.android

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
val requiresBackgroundLocationPermission: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
val backgroundLocationRequiresSettings: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
