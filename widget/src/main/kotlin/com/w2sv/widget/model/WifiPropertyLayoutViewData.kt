package com.w2sv.widget.model

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.w2sv.androidutils.appwidgets.setBackgroundColor
import com.w2sv.domain.model.IPAddress
import com.w2sv.widget.R
import com.w2sv.widget.utils.setTextView

internal sealed interface WifiPropertyLayoutViewData {

    fun inflateView(context: Context, widgetColors: WidgetColors): RemoteViews

    data class WifiProperty(val label: CharSequence, val value: CharSequence) :
        WifiPropertyLayoutViewData {

        override fun inflateView(context: Context, widgetColors: WidgetColors): RemoteViews =
            RemoteViews(context.packageName, R.layout.wifi_property)
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
    }

    data class IPProperties(
        val ipAddress: IPAddress,
        val showPrefixLength: Boolean,
    ) : WifiPropertyLayoutViewData {

        override fun inflateView(context: Context, widgetColors: WidgetColors): RemoteViews =
            RemoteViews(context.packageName, R.layout.ip_properties)
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

                    if (showPrefixLength) {
                        addSubPropertyTV("${ipAddress.prefixLength}", R.id.ip_property_tv_1)
                    } else {
                        setViewVisibility(R.id.ip_property_tv_1, View.GONE)
                    }
                }
    }
}
