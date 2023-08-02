package com.w2sv.widget.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.data.connectivityManager
import com.w2sv.data.storage.WidgetRepository
import com.w2sv.data.wifiManager
import com.w2sv.widget.R
import com.w2sv.widget.data.appearance
import com.w2sv.widget.model.WidgetColors
import com.w2sv.widget.model.WifiPropertyView
import com.w2sv.widget.utils.setTextView
import dagger.hilt.android.qualifiers.ApplicationContext
import slimber.log.i
import javax.inject.Inject

class WifiPropertyViewsFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetRepository: WidgetRepository
) : RemoteViewsService.RemoteViewsFactory {

    private val wifiManager: WifiManager by lazy { context.wifiManager }
    private val connectivityManager: ConnectivityManager by lazy { context.connectivityManager }

    override fun onCreate() {}

    private lateinit var propertyViewData: List<WifiPropertyView>
    private lateinit var widgetColors: WidgetColors

    override fun onDataSetChanged() {
        i { "${this::class.simpleName}.onDataSetChanged" }

        propertyViewData = widgetRepository.getSetWifiProperties()
            .map {
                WifiPropertyView(
                    context.getString(it.labelRes),
                    it.getValue(wifiManager, connectivityManager)
                )
            }

        widgetColors = widgetRepository.appearance.getValueSynchronously().theme.getColors(context)
    }

    override fun getCount(): Int = propertyViewData.size

    override fun getViewAt(position: Int): RemoteViews =
        RemoteViews(context.packageName, R.layout.wifi_property)
            .apply {
                setTextView(
                    viewId = R.id.property_label_tv,
                    text = propertyViewData[position].label,
                    color = widgetColors.primary
                )
                setTextView(
                    viewId = R.id.property_value_tv,
                    text = propertyViewData[position].value,
                    color = widgetColors.secondary
                )
            }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long =
        propertyViewData[position].hashCode().toLong()

    override fun hasStableIds(): Boolean = true

    override fun onDestroy() {}
}