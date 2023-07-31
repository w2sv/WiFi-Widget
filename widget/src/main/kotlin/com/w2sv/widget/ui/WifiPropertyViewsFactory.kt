package com.w2sv.widget.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import com.w2sv.common.connectivityManager
import com.w2sv.common.data.model.WidgetAppearance
import com.w2sv.common.data.model.WidgetColors
import com.w2sv.common.data.model.WifiProperty
import com.w2sv.common.wifiManager
import com.w2sv.widget.R
import com.w2sv.widget.ui.model.WifiPropertyView
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WifiPropertyViewsFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val setWifiProperties: Set<WifiProperty>,
    private val widgetAppearance: WidgetAppearance
) : RemoteViewsService.RemoteViewsFactory {

    private val wifiManager: WifiManager by lazy { context.wifiManager }
    private val connectivityManager: ConnectivityManager by lazy { context.connectivityManager }

    override fun onCreate() {}

    private lateinit var propertyViewData: List<WifiPropertyView>
    private lateinit var widgetColors: WidgetColors

    override fun onDataSetChanged() {
        propertyViewData = setWifiProperties
            .map {
                WifiPropertyView(
                    context.getString(it.labelRes),
                    it.getValue(wifiManager, connectivityManager)
                )
            }
        widgetColors = widgetAppearance.theme.getColors(context)
    }

    override fun getCount(): Int = propertyViewData.size

    override fun getViewAt(position: Int): RemoteViews =
        RemoteViews(context.packageName, R.layout.wifi_property)
            .apply {
                setTextView(
                    viewId = R.id.property_label_tv,
                    text = propertyViewData[position].label,
                    color = widgetColors.labels
                )
                setTextView(
                    viewId = R.id.property_value_tv,
                    text = propertyViewData[position].value,
                    color = widgetColors.other
                )
            }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long =
        propertyViewData[position].hashCode().toLong()

    override fun hasStableIds(): Boolean = true

    override fun onDestroy() {}
}

private fun RemoteViews.setTextView(@IdRes viewId: Int, text: String, @ColorInt color: Int) {
    setTextViewText(viewId, text)
    setTextColor(viewId, color)
}