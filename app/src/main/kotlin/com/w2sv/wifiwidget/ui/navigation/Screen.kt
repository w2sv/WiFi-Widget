package com.w2sv.wifiwidget.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen : NavKey {
    @Serializable
    object Home : Screen()

    @Serializable
    object WidgetConfiguration : Screen()
}
