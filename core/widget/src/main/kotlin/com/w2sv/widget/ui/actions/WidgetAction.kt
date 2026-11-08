package com.w2sv.widget.ui.actions

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionRunCallback
import com.w2sv.core.resources.R
import com.w2sv.domain.widget.WidgetUtility

internal enum class WidgetAction(
    val configurationKey: WidgetUtility,
    @DrawableRes val iconRes: Int,
    @StringRes val descriptionRes: Int,
    val createAction: (Context) -> Action?
) {
    Refresh(
        WidgetUtility.RefreshButton,
        R.drawable.ic_refresh_24,
        R.string.refresh_data,
        { actionRunCallback<RefreshWidgetAction>() }
    ),
    WifiSettings(
        WidgetUtility.GoToWifiSettingsButton,
        R.drawable.ic_wifi_settings_24,
        R.string.open_wifi_settings_button,
        { context -> wifiSettingsAction(context) }
    )
}
