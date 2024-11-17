package com.w2sv.common.utils

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@ChecksSdkIntAtLeast(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
inline fun onAtLeastAndroidU(block: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        block()
    }
}

@ChecksSdkIntAtLeast(Build.VERSION_CODES.R)
inline fun onAtLeastAndroidR(block: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        block()
    }
}
