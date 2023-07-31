package com.w2sv.widget.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.common.connectivityManager
import com.w2sv.common.data.storage.WidgetConfigurationRepository
import com.w2sv.common.wifiManager
import com.w2sv.widget.R
import com.w2sv.widget.ui.model.WifiPropertyView
import com.w2sv.widget.ui.model.WifiPropertyViewsColors
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WifiPropertyViewsFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetConfigurationRepository: WidgetConfigurationRepository
) : RemoteViewsService.RemoteViewsFactory {

    private val wifiManager: WifiManager by lazy { context.wifiManager }
    private val connectivityManager: ConnectivityManager by lazy { context.connectivityManager }

    override fun onCreate() {}

    private lateinit var propertyViewData: List<WifiPropertyView>
    private lateinit var propertyViewColors: WifiPropertyViewsColors

    override fun onDataSetChanged() {
        propertyViewData = widgetConfigurationRepository.wifiProperties.getSynchronousMap()
            .filterValues { it }
            .keys
            .map {
                WifiPropertyView(
                    context.getString(it.labelRes),
                    it.getValue(wifiManager, connectivityManager)
                )
            }
        propertyViewColors = WifiPropertyViewsColors.get(
            widgetConfigurationRepository.theme.getValueSynchronously(),
            widgetConfigurationRepository.customColors,
            context
        )
    }

    override fun getCount(): Int = propertyViewData.size

    override fun getViewAt(position: Int): RemoteViews =
        RemoteViews(context.packageName, R.layout.wifi_property)
            .apply {
                setTextView(
                    R.id.property_label_tv,
                    propertyViewData[position].label,
                    propertyViewColors.label
                )
                setTextView(
                    R.id.property_value_tv,
                    propertyViewData[position].value,
                    propertyViewColors.value
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