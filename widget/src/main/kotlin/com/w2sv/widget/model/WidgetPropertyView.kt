package com.w2sv.widget.model

import android.content.res.ColorStateList
import android.os.Build
import android.widget.RemoteViews
import com.w2sv.androidutils.appwidgets.setBackgroundColor
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.widget.R
import com.w2sv.widget.utils.setTextView

internal sealed interface WidgetPropertyView {
    class Property(val property: WidgetWifiProperty.ValueViewData) : WidgetPropertyView
    class PrefixLength(val value: String) : WidgetPropertyView

    fun inflate(
        packageName: String,
        widgetColors: WidgetColors
    ): RemoteViews =
        when (this) {
            is Property -> {
                inflateWifiPropertyLayout(packageName, property.label, property.value, widgetColors)
            }

            is PrefixLength -> {
                inflatePrefixLengthLayout(packageName, value, widgetColors)
            }
        }
}

private fun inflateWifiPropertyLayout(
    packageName: String,
    label: String,
    value: String,
    widgetColors: WidgetColors
): RemoteViews =
    RemoteViews(packageName, R.layout.wifi_property)
        .apply {
            setTextView(
                viewId = R.id.property_label_tv,
                text = label,
                color = widgetColors.primary,
            )
            setTextView(
                viewId = R.id.property_value_tv,
                text = value,
                color = widgetColors.secondary,
            )
        }

private fun inflatePrefixLengthLayout(
    packageName: String,
    prefixLengthText: String,
    widgetColors: WidgetColors
): RemoteViews =
    RemoteViews(packageName, R.layout.ip_properties)
        .apply {
            fun addSubPropertyTV(text: String, textViewId: Int) {
                setTextView(
                    viewId = textViewId,
                    text = text,
                    color = widgetColors.secondary,
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setColorStateList(
                        viewId,
                        "setBackgroundTintList",
                        ColorStateList.valueOf(widgetColors.ipSubPropertyBackgroundColor),
                    )
                } else {
                    setBackgroundColor(
                        textViewId,
                        widgetColors.ipSubPropertyBackgroundColor
                    )
                }
            }

            addSubPropertyTV(prefixLengthText, R.id.ip_property_tv_1)
        }
