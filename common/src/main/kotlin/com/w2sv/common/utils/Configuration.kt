package com.w2sv.common.utils

import android.content.res.Configuration

val Configuration.isNightModeActiveCompat: Boolean
    get() = uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES