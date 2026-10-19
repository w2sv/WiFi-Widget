package com.w2sv.widget.ui.properties.renderdata

import android.content.Context
import com.w2sv.core.widget.R
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.widget.WidgetAppearance
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.domain.widget.WifiPropertyValueAlignment
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentationProvider
import com.w2sv.widget.ui.resolve
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import slimber.log.d

internal class WifiPropertyRenderDataProvider @Inject constructor(
    private val widgetConfigSource: WidgetConfigSource,
    private val remoteNetworkDataRepository: RemoteNetworkDataRepository,
    private val wifiPropertyPresentationProvider: WifiPropertyPresentationProvider
) {
    operator fun invoke(context: Context): WifiPropertyRenderData {
        val config = widgetConfigSource.blockingSnapshot()
        val propertyPresentations = runBlocking {
            wifiPropertyPresentationProvider(
                enabledProperties = config.enabledProperties,
                enabledIpSettings = config::enabledIpSettings,
                remoteNetworkData = remoteNetworkDataRepository.reconciledSnapshot(config.remoteDataConfig)
            )
        }
        d { "propertyPresentations=$propertyPresentations" }

        return WifiPropertyRenderData(
            presentations = propertyPresentations,
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
