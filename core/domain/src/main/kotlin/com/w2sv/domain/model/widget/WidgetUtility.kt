package com.w2sv.domain.model.widget

import androidx.annotation.StringRes
import com.w2sv.core.common.R
import com.w2sv.domain.model.Labelled

enum class WidgetUtility(@StringRes override val labelRes: Int, @StringRes val explanation: Int) : Labelled {
    RefreshTimeDisplay(R.string.refresh_time_display, R.string.refresh_time_display_explanation),
    RefreshButton(R.string.refresh_button, R.string.refresh_button_explanation),
    GoToWifiSettingsButton(R.string.open_wifi_settings_button, R.string.go_to_wifi_settings_button_explanation),
    GoToWidgetSettingsButton(R.string.open_widget_settings_button, R.string.go_to_widget_settings_button_explanation)
}
