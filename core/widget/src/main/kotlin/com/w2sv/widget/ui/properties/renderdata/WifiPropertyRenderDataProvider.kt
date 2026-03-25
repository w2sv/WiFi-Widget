package com.w2sv.widget.ui.properties.renderdata

import android.content.Context
import com.w2sv.common.utils.log
import com.w2sv.core.widget.R
import com.w2sv.domain.model.widget.WidgetAppearance
import com.w2sv.domain.model.widget.WifiPropertyValueAlignment
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.domain.repository.RemoteWifiDataRepository
import com.w2sv.domain.repository.WidgetConfigFlow
import com.w2sv.widget.ui.resolve
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

internal class WifiPropertyRenderDataProvider @Inject constructor(
    private val widgetConfigFlow: WidgetConfigFlow,
    private val remoteWifiDataRepository: RemoteWifiDataRepository,
    private val wifiPropertyViewDataProvider: WifiPropertyViewDataProvider
) {
    operator fun invoke(context: Context): WifiPropertyRenderData {
        val config = runBlocking { widgetConfigFlow.first() }

        return WifiPropertyRenderData(
            viewData = runBlocking {
                wifiPropertyViewDataProvider(
                    enabledProperties = config.enabledProperties,
                    enabledIpSettings = config::enabledIpSettings,
                    remoteWifiData = remoteWifiDataRepository.data.value
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
