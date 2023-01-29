package com.w2sv.wifiwidget.utils

import android.content.Intent

fun Intent.getIntExtraOrNull(name: String, defaultValue: Int): Int? =
    getIntExtra(name, defaultValue).run {
        if (equals(defaultValue))
            null
        else
            this
    }

fun Intent.setMakeUniqueActivityFlags(): Intent =
    setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)