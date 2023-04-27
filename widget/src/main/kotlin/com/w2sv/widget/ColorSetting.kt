package com.w2sv.widget

import android.content.Context
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.w2sv.androidutils.appwidgets.setBackgroundColor
import com.w2sv.androidutils.appwidgets.setColorFilter
import com.w2sv.common.enums.Theme
import com.w2sv.common.enums.WidgetColorSection
import com.w2sv.common.extensions.getDeflowedMap
import com.w2sv.common.extensions.isNightModeActiveCompat
import com.w2sv.common.extensions.toRGBChannelInt
import kotlinx.coroutines.flow.Flow

internal fun RemoteViews.setWidgetColors(
    theme: Theme,
    customWidgetColors: Map<WidgetColorSection, Flow<Int>>,
    backgroundOpacity: Float,
    context: Context
) {
    when (theme) {
        Theme.Dark -> setColors(
            context.getColor(android.R.color.background_dark),
            backgroundOpacity,
            context.getColor(androidx.appcompat.R.color.foreground_material_dark)
        )

        Theme.DeviceDefault -> {
            when (context.resources.configuration.isNightModeActiveCompat) {
                false -> setWidgetColors(
                    Theme.Light,
                    customWidgetColors,
                    backgroundOpacity,
                    context
                )

                true -> setWidgetColors(
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
            context.getColor(androidx.appcompat.R.color.foreground_material_light)
        )

        Theme.Custom -> with(customWidgetColors.getDeflowedMap()) {
            setColors(
                getValue(WidgetColorSection.Background),
                backgroundOpacity,
                getValue(WidgetColorSection.Values)
            )
        }
    }
}

private fun RemoteViews.setColors(
    @ColorInt background: Int,
    backgroundOpacity: Float,
    @ColorInt foreground: Int
) {
    // Background
    setBackgroundColor(
        R.id.widget_layout,
        ColorUtils.setAlphaComponent(
            background,
            backgroundOpacity.toRGBChannelInt()
        )
    )

    // TVs
    setTextColor(R.id.wifi_status_tv, foreground)
    setTextColor(R.id.last_updated_tv, foreground)

    // ImageButtons
    setColorFilter(R.id.settings_button, foreground)
    setColorFilter(R.id.refresh_button, foreground)
}