package com.w2sv.widget.ui.model

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.common.data.sources.Theme
import com.w2sv.common.data.sources.WidgetColor
import com.w2sv.common.extensions.isNightModeActiveCompat
import com.w2sv.widget.R
import kotlinx.coroutines.flow.Flow

internal data class WifiPropertyViewsColors(@ColorInt val label: Int, @ColorInt val value: Int) {

    override fun toString(): String {
        val labelColor = Color.valueOf(label)
        val valueColor = Color.valueOf(value)

        return "Label: ${labelColor.red()} ${labelColor.green()} ${labelColor.blue()} | " +
                "Value: ${valueColor.red()} ${valueColor.green()} ${valueColor.blue()}"
    }

    companion object {
        fun get(
            theme: Theme,
            customWidgetColors: Map<WidgetColor, Flow<Int>>,
            context: Context
        ): WifiPropertyViewsColors =
            when (theme) {
                Theme.Light -> WifiPropertyViewsColors(
                    context.getColor(R.color.default_label),
                    context.getColor(R.color.foreground_light)
                )

                Theme.Dark -> WifiPropertyViewsColors(
                    context.getColor(R.color.default_label),
                    context.getColor(R.color.foreground_dark)
                )

                Theme.DeviceDefault -> get(
                    when (context.resources.configuration.isNightModeActiveCompat) {
                        false -> Theme.Light
                        true -> Theme.Dark
                    },
                    customWidgetColors,
                    context
                )

                Theme.Custom -> customWidgetColors.getSynchronousMap().run {
                    WifiPropertyViewsColors(
                        getValue(WidgetColor.Labels),
                        getValue(WidgetColor.Other)
                    )
                }
            }
    }
}