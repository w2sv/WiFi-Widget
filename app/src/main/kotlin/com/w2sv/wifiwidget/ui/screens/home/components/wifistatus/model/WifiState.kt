package com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.model

import androidx.compose.runtime.Immutable
import com.w2sv.domain.model.WifiViewData
import com.w2sv.domain.model.WifiStatus
import kotlinx.coroutines.flow.Flow

@Immutable
sealed class WifiState(val status: WifiStatus) {
    val connectedOrNull
        get() = this as? Connected

    data object Disabled : WifiState(WifiStatus.Disabled)
    data object Disconnected : WifiState(WifiStatus.Disconnected)
    data class Connected(val wifiViewData: List<WifiViewData>) : WifiState(WifiStatus.Connected)
}
