package com.w2sv.widget.ui.properties

import android.content.Context
import com.w2sv.common.utils.log
import com.w2sv.core.widget.R
import com.w2sv.domain.model.widget.WidgetAppearance
import com.w2sv.domain.model.widget.WifiPropertyValueAlignment
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.domain.repository.RemoteNetworkInfoRepository
import com.w2sv.domain.repository.WidgetConfigFlow
import com.w2sv.widget.ui.resolve
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

internal class WifiPropertyRenderDataProvider @Inject constructor(
    private val widgetConfigFlow: WidgetConfigFlow,
    private val remoteNetworkInfoRepository: RemoteNetworkInfoRepository,
    private val wifiPropertyViewDataProvider: WifiPropertyViewDataProvider
) {
    operator fun invoke(context: Context): WifiPropertyRenderData {
        val config = runBlocking { widgetConfigFlow.first() }

        return WifiPropertyRenderData(
            viewData = runBlocking {
                wifiPropertyViewDataProvider(
                    enabledProperties = config.orderedEnabledProperties,
                    enabledIpSettings = config::enabledIpSettings,
                    remoteNetworkInfo = remoteNetworkInfoRepository.data.value
                )
                    .log { "propertyViewData=$it" }
            },
            colors = config.appearance.coloring.resolve(context),
            fontSize = config.appearance.fontSize,
            layout = config.appearance.propertyLayout
        )
    }
}

private val WidgetAppearance.propertyLayout: Int
    get() = when (propertyValueAlignment) {
        WifiPropertyValueAlignment.Left -> R.layout.wifi_property_left_aligned
        WifiPropertyValueAlignment.Right -> R.layout.wifi_property_right_aligned
    }
