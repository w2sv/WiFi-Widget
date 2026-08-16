package com.w2sv.widget.ui.state

import android.content.Context
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.domain.repository.RemoteWifiDataRepository
import com.w2sv.domain.repository.WidgetConfigFlow
import com.w2sv.networking.wifistatus.provider.WifiStatusProvider
import com.w2sv.widget.ui.model.WifiWidgetState
import com.w2sv.widget.ui.theme.resolve
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first

internal class WifiWidgetStateProvider @Inject constructor(
    private val widgetConfigFlow: WidgetConfigFlow,
    private val remoteWifiDataRepository: RemoteWifiDataRepository,
    private val wifiPropertyViewDataProvider: WifiPropertyViewDataProvider,
    private val wifiStatusProvider: WifiStatusProvider
) {
    suspend operator fun invoke(context: Context): WifiWidgetState {
        val config = widgetConfigFlow.first()
        val status = wifiStatusProvider()
        val properties = if (status.isConnected) {
            wifiPropertyViewDataProvider(
                enabledProperties = config.enabledProperties,
                enabledIpSettings = config::enabledIpSettings,
                remoteWifiData = remoteWifiDataRepository.data.value
            )
        } else {
            emptyList()
        }

        return WifiWidgetState(
            config = config,
            colors = config.appearance.coloring.resolve(context),
            status = status,
            properties = properties,
            renderedAt = Instant.now()
        )
    }
}
