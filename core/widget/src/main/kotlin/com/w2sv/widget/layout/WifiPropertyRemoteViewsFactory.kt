package com.w2sv.widget.layout

import android.content.Context
import android.widget.RemoteViews
import com.w2sv.common.utils.log
import com.w2sv.core.widget.R
import com.w2sv.domain.model.widget.Alignment
import com.w2sv.domain.model.widget.FontSize
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewData
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.domain.repository.RemoteNetworkInfoRepository
import com.w2sv.domain.repository.WidgetConfigFlow
import com.w2sv.widget.utils.logging.LoggingRemoteViewsFactory
import com.w2sv.widget.utils.remoteViews
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.properties.Delegates

internal class WifiPropertyRemoteViewsFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetConfigFlow: WidgetConfigFlow,
    private val remoteNetworkInfoRepository: RemoteNetworkInfoRepository,
    private val wifiPropertyViewDataProvider: WifiPropertyViewDataProvider
) : LoggingRemoteViewsFactory() {

    private lateinit var wifiPropertyViewData: List<WifiPropertyViewData>
    private lateinit var widgetColors: WidgetColors
    private lateinit var fontSize: FontSize
    private var propertyLayout by Delegates.notNull<Int>()

    override fun onCreate() {
        super.onCreate()
        updateViewData()
    }

    override fun onDataSetChanged() {
        super.onDataSetChanged()
        updateViewData()
    }

    private fun updateViewData() {
        val widgetConfig = runBlocking { widgetConfigFlow.first() }

        wifiPropertyViewData = wifiPropertyViewDataProvider(
            enabledProperties = widgetConfig.orderedEnabledProperties(),
            enabledIpSettings = { property -> widgetConfig.enabledIpSettings(property) },
            remoteNetworkInfo = remoteNetworkInfoRepository.data.value
        )
            .log { "Set propertyViewData=$it" }

        widgetColors = widgetConfig.appearance.resolvedWidgetColors(context)
        fontSize = widgetConfig.appearance.fontSize
        propertyLayout = when (widgetConfig.appearance.propertyValueAlignment) {
            Alignment.Left -> R.layout.wifi_property_left_aligned
            Alignment.Right -> R.layout.wifi_property_right_aligned
        }
    }

    override fun getCount(): Int =
        wifiPropertyViewData.size

    override fun getViewAt(position: Int): RemoteViews {
        val remoteViews = remoteViews(context, propertyLayout)

        val viewData = wifiPropertyViewData.getOrNull(position)
            ?: return remoteViews

        remoteViews.inflatePropertyLayout(
            viewData = viewData.log { "Inflating property layout for $it" },
            widgetColors = widgetColors,
            fontSize = fontSize
        )

        return remoteViews
    }

    override fun getLoadingView(): RemoteViews =
        remoteViews(context, R.layout.loading)
            .apply {
                setTextColor(R.id.loading_tv, widgetColors.secondary)
            }

    override fun getViewTypeCount(): Int =
        2

    override fun getItemId(position: Int): Long =
        wifiPropertyViewData.getOrNull(position)?.hashCode()?.toLong() ?: -1L

    override fun hasStableIds(): Boolean =
        true
}
