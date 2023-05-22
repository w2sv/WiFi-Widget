package com.w2sv.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.common.connectivityManager
import com.w2sv.common.datastore.DataStoreRepository
import com.w2sv.common.enums.Theme
import com.w2sv.common.enums.WidgetColorSection
import com.w2sv.common.extensions.getDeflowedMap
import com.w2sv.common.extensions.isNightModeActiveCompat
import com.w2sv.common.wifiManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@AndroidEntryPoint
class WifiPropertiesService : RemoteViewsService() {

    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        WifiPropertyViewFactory(applicationContext, dataStoreRepository)
}

private class WifiPropertyViewFactory(
    private val context: Context,
    private val dataStoreRepository: DataStoreRepository
) : RemoteViewsService.RemoteViewsFactory {

    private lateinit var wifiManager: WifiManager
    private lateinit var connectivityManager: ConnectivityManager

    override fun onCreate() {
        wifiManager = context.wifiManager
        connectivityManager = context.connectivityManager
    }

    private lateinit var propertyViewData: List<WifiPropertyViewData>
    private lateinit var propertyViewColors: WifiPropertyViewColors

    override fun onDataSetChanged() {
        propertyViewData = dataStoreRepository.wifiProperties.getDeflowedMap()
            .filterValues { it }
            .keys
            .map {
                WifiPropertyViewData(
                    context.getString(it.labelRes),
                    it.getValue(wifiManager, connectivityManager)
                )
            }
        propertyViewColors = WifiPropertyViewColors.fromTheme(
            dataStoreRepository.widgetTheme.getValueSynchronously(),
            context,
            dataStoreRepository.customWidgetColors
        )
    }

    override fun onDestroy() {}

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
}

private fun RemoteViews.setTextView(@IdRes viewId: Int, text: String, @ColorInt color: Int) {
    setTextViewText(viewId, text)
    setTextColor(viewId, color)
}

private data class WifiPropertyViewData(val label: String, val value: String)

private data class WifiPropertyViewColors(val label: Int, val value: Int) {

    companion object {
        fun fromTheme(
            theme: Theme,
            context: Context,
            customWidgetColors: Map<WidgetColorSection, Flow<Int>>
        ): WifiPropertyViewColors =
            when (theme) {
                Theme.Light -> WifiPropertyViewColors(
                    context.getColor(com.w2sv.common.R.color.blue_chill),
                    context.getColor(androidx.appcompat.R.color.foreground_material_light)
                )

                Theme.Dark -> WifiPropertyViewColors(
                    context.getColor(com.w2sv.common.R.color.blue_chill),
                    context.getColor(androidx.appcompat.R.color.foreground_material_dark)
                )

                Theme.DeviceDefault -> fromTheme(
                    when (context.resources.configuration.isNightModeActiveCompat) {
                        false -> Theme.Light
                        true -> Theme.Dark
                    },
                    context,
                    customWidgetColors
                )

                Theme.Custom -> customWidgetColors.getDeflowedMap().run {
                    WifiPropertyViewColors(
                        getValue(WidgetColorSection.Labels),
                        getValue(WidgetColorSection.Values)
                    )
                }
            }
    }

    override fun toString(): String {
        val labelColor = Color.valueOf(label)
        val valueColor = Color.valueOf(value)

        return "Label: ${labelColor.red()} ${labelColor.green()} ${labelColor.blue()} | " +
                "Value: ${valueColor.red()} ${valueColor.green()} ${valueColor.blue()}"
    }
}