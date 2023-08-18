package com.w2sv.wifiwidget.ui.utils

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration

fun Modifier.conditional(
    condition: Boolean,
    onTrue: Modifier.() -> Modifier,
    onFalse: (Modifier.() -> Modifier)? = null
): Modifier {
    return if (condition) {
        then(onTrue(Modifier))
    } else {
        onFalse?.invoke(Modifier) ?: this
    }
}

val landscapeModeActivated: Boolean
    @Composable get() = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE