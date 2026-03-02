package com.w2sv.wifiwidget.ui.screen.home.wifistatus.model

import androidx.compose.runtime.Immutable
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewData
import com.w2sv.domain.model.networking.WifiStatus

@Immutable
sealed class WifiState(val status: WifiStatus) {
    val connectedOrNull
        get() = this as? Connected

    data object Disabled : WifiState(WifiStatus.Disabled)
    data object Disconnected : WifiState(WifiStatus.Disconnected)
    data class Connected(val wifiPropertyViewData: List<WifiPropertyViewData>) : WifiState(WifiStatus.Connected)
}
