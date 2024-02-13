package com.w2sv.domain.model

import androidx.annotation.StringRes
import com.w2sv.domain.R

enum class WidgetBottomRowElement(@StringRes override val labelRes: Int) : WidgetProperty {
    LastRefreshTimeDisplay(R.string.display_last_refresh_time),
    RefreshButton(R.string.refresh_button),
    GoToWifiSettingsButton(R.string.open_wifi_settings_button),
    GoToWidgetSettingsButton(R.string.open_widget_settings_button)
}
