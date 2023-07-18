package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import android.os.Build

internal val backgroundLocationAccessGrantRequired: Boolean get() =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q