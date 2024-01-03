package com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.model

import androidx.compose.runtime.Immutable
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.model.WifiStatus
import kotlinx.coroutines.flow.Flow

@Immutable
sealed class WifiState(
    val status: WifiStatus
) {
    data object Disabled : WifiState(WifiStatus.Disabled)
    data object Disconnected : WifiState(WifiStatus.Disconnected)
    class Connected(val propertyViewData: Flow<WidgetWifiProperty.ViewData>) :
        WifiState(WifiStatus.Connected)
}