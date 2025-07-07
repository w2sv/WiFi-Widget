package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.w2sv.domain.model.WidgetColoring

enum class CustomWidgetColor(@param:StringRes val labelRes: Int) {
    Background(com.w2sv.core.domain.R.string.background),
    Primary(com.w2sv.core.domain.R.string.primary),
    Secondary(com.w2sv.core.domain.R.string.secondary);

    fun getColor(data: WidgetColoring.Style.Custom): Color =
        Color(
            when (this) {
                Background -> data.background
                Primary -> data.primary
                Secondary -> data.secondary
            }
        )
}
