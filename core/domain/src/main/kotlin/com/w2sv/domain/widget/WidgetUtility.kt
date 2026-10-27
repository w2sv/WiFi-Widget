package com.w2sv.domain.widget

import androidx.annotation.StringRes
import com.w2sv.core.resources.R
import com.w2sv.domain.WithLabel

enum class WidgetUtility(val protoId: Int, @StringRes override val labelRes: Int, @StringRes val explanation: Int) : WithLabel {
    RefreshTimeDisplay(0, R.string.refresh_time_display, R.string.refresh_time_display_explanation),
    RefreshButton(1, R.string.refresh_button, R.string.refresh_button_explanation),
    GoToWifiSettingsButton(2, R.string.open_wifi_settings_button, R.string.go_to_wifi_settings_button_explanation)
}
