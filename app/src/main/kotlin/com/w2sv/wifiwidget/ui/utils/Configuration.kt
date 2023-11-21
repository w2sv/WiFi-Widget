package com.w2sv.wifiwidget.ui.utils

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

val isLandscapeModeActivated: Boolean
    @Composable get() = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE