package com.w2sv.domain.model

import androidx.annotation.StringRes
import com.w2sv.domain.R

enum class WidgetButton(@StringRes override val labelRes: Int) : WidgetProperty {
    Refresh(R.string.refresh),
    GoToWifiSettings(R.string.go_to_wifi_settings),
    GoToWidgetSettings(R.string.go_to_widget_settings)
}
