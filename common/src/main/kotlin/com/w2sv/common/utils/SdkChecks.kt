package com.w2sv.common.utils

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
val dynamicColorsSupported
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S