package com.w2sv.wifiwidget.utils

import android.content.res.Resources
import androidx.annotation.ArrayRes

fun Resources.getNestedStringArray(@ArrayRes id: Int, index: Int): List<String> =
    obtainTypedArray(id).run {
        getTextArray(index).map { it.toString() }
            .also { recycle() }
    }