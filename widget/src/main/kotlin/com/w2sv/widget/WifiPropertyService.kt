package com.w2sv.widget

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.w2sv.common.datastore.DataStoreRepository
import com.w2sv.common.extensions.getDeflowedMap
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WifiPropertyService : RemoteViewsService() {

    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        WifiPropertyViewFactory(applicationContext, dataStoreRepository)
}

private class WifiPropertyViewFactory(
    private val context: Context,
    private val dataStoreRepository: DataStoreRepository
) : RemoteViewsService.RemoteViewsFactory {

    private lateinit var propertyViewData: List<WifiPropertyViewData>

    override fun onCreate() {
        // In onCreate(), set up any connections or cursors to your data
        // source. Heavy lifting, such as downloading or creating content,
        // must be deferred to onDataSetChanged() or getViewAt(). Taking
        // more than 20 seconds on this call results in an ANR.
        val wifiManager = context.getSystemService(WifiManager::class.java)

        propertyViewData = dataStoreRepository.wifiProperties.getDeflowedMap()
            .filterValues { it }
            .keys
            .map {
                WifiPropertyViewData(
                    context.getString(it.labelRes),
                    it.getValue(wifiManager)
                )
            }
    }

    override fun onDataSetChanged() {}

    override fun onDestroy() {}

    override fun getCount(): Int = propertyViewData.size

    override fun getViewAt(position: Int): RemoteViews =
        RemoteViews(context.packageName, R.layout.wifi_property)
            .apply {
                with(propertyViewData[position]) {
                    setTextViewText(R.id.property_label_tv, label)
                    setTextViewText(R.id.property_value_tv, value)
                }
            }

    override fun getLoadingView(): RemoteViews =
        RemoteViews(context.packageName, R.layout.wifi_property)

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long =
        propertyViewData[position].hashCode().toLong()

    override fun hasStableIds(): Boolean = true
}

private class WifiPropertyViewData(val label: String, val value: String)