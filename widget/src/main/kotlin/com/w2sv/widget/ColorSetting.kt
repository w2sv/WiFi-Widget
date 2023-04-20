package com.w2sv.widget

import android.content.Context
import android.content.res.Configuration
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.core.graphics.ColorUtils
import com.w2sv.androidutils.appwidgets.setBackgroundColor
import com.w2sv.androidutils.appwidgets.setColorFilter
import com.w2sv.common.Theme
import com.w2sv.common.WidgetColorSection
import com.w2sv.common.extensions.toRGBChannelInt

internal fun RemoteViews.setWidgetColors(
    theme: Theme,
    customWidgetColors: Lazy<Map<WidgetColorSection, Int>>,
    backgroundOpacity: Float,
    context: Context
) {
    when (theme) {
        Theme.Dark -> setColors(
            context.getColor(android.R.color.background_dark),
            backgroundOpacity,
            context.getColor(com.w2sv.common.R.color.blue_chill),
            context.getColor(androidx.appcompat.R.color.foreground_material_dark)
        )

        Theme.DeviceDefault -> {
            when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> setWidgetColors(
                    Theme.Light,
                    customWidgetColors,
                    backgroundOpacity,
                    context
                )

                Configuration.UI_MODE_NIGHT_YES -> setWidgetColors(
                    Theme.Dark,
                    customWidgetColors,
                    backgroundOpacity,
                    context
                )
            }
        }

        Theme.Light -> setColors(
            context.getColor(android.R.color.background_light),
            backgroundOpacity,
            context.getColor(com.w2sv.common.R.color.blue_chill),
            context.getColor(androidx.appcompat.R.color.foreground_material_light)
        )

        Theme.Custom -> with(customWidgetColors.value) {
            setColors(
                getValue(WidgetColorSection.Background),
                backgroundOpacity,
                getValue(WidgetColorSection.Labels),
                getValue(WidgetColorSection.Values)
            )
        }
    }
}

private fun RemoteViews.setColors(
    @ColorInt background: Int,
    backgroundOpacity: Float,
    @ColorInt labels: Int,
    @ColorInt values: Int
) {
    // Background
    setBackgroundColor(
        R.id.widget_layout,
        ColorUtils.setAlphaComponent(
            background,
            backgroundOpacity.toRGBChannelInt()
        )
    )

    // Labels
    listOf(
        R.id.ssid_tv,
        R.id.ip_tv,
        R.id.frequency_tv,
        R.id.linkspeed_tv,
        R.id.gateway_tv,
        R.id.dhcp_tv,
        R.id.dns_tv,
        R.id.netmask_tv
    )
        .forEach {
            setTextColor(it, labels)
        }

    // 'Other' TVs
    listOf(
        R.id.ssid_value_tv,
        R.id.ip_value_tv,
        R.id.frequency_value_tv,
        R.id.linkspeed_value_tv,
        R.id.gateway_value_tv,
        R.id.dhcp_value_tv,
        R.id.dns_value_tv,
        R.id.netmask_value_tv,

        R.id.wifi_status_tv,
        R.id.go_to_wifi_settings_tv,
        R.id.last_updated_tv
    )
        .forEach {
            setTextColor(it, values)
        }

    // 'Other' ImageButtons
    listOf(
        R.id.settings_button,
        R.id.refresh_button
    )
        .forEach {
            setColorFilter(it, values)
        }
}