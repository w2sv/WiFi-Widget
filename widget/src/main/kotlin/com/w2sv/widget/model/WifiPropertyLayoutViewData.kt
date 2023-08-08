package com.w2sv.widget.model

import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.w2sv.data.networking.IPAddress
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
                        color = widgetColors.primary
                    )
                    setTextView(
                        viewId = R.id.property_value_tv,
                        text = value,
                        color = widgetColors.secondary
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
                    val propertyTVIterator = listOf(
                        R.id.ip_property_tv_1,
                        R.id.ip_property_tv_2,
                        R.id.ip_property_tv_3,
                        R.id.ip_property_tv_4,
                        R.id.ip_property_tv_5
                    )
                        .iterator()

                    fun addSubPropertyTV(text: String) {
                        setTextView(
                            viewId = propertyTVIterator.next(),
                            text = text,
                            color = widgetColors.secondary
                        )
                    }

                    fun addSubPropertyTVIfConditionMet(conditional: Boolean, text: String) {
                        if (conditional) {
                            addSubPropertyTV(text)
                        }
                    }

                    // Prefix length
                    addSubPropertyTVIfConditionMet(
                        showPrefixLength, "/${ipAddress.prefixLength}"
                    )

                    // Local / Public
                    if (ipAddress.isLocal) {
                        addSubPropertyTVIfConditionMet(
                            ipAddress.localAttributes.siteLocal, "SiteLocal"
                        )
                        addSubPropertyTVIfConditionMet(
                            ipAddress.localAttributes.linkLocal, "LinkLocal"
                        )
                        addSubPropertyTVIfConditionMet(
                            !ipAddress.localAttributes.siteLocal && !ipAddress.localAttributes.linkLocal,
                            "Local"
                        )
                    } else {
                        addSubPropertyTV("Public")
                    }

                    // Additional properties
                    addSubPropertyTVIfConditionMet(ipAddress.isLoopback, "Loopback")
                    addSubPropertyTVIfConditionMet(ipAddress.isMultiCast, "Multicast")

                    // Hide all remaining sub-property views
                    propertyTVIterator.forEachRemaining {
                        setViewVisibility(it, View.GONE)
                    }
                }
    }
}