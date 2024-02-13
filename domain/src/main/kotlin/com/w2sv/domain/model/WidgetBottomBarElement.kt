package com.w2sv.domain.model

import androidx.annotation.StringRes
import com.w2sv.domain.R

enum class WidgetBottomBarElement(@StringRes override val labelRes: Int) : WidgetProperty {
    LastRefreshTimeDisplay(R.string.display_last_refresh_time),
    RefreshButton(R.string.refresh),
    GoToWifiSettingsButton(R.string.go_to_wifi_settings),
    GoToWidgetSettingsButton(R.string.go_to_widget_settings)
}
