package com.w2sv.wifiwidget.ui.screen.home.model

import androidx.compose.runtime.Immutable
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewData

@Immutable
sealed class WifiState(val status: WifiStatus) {
    val asConnectedOrNull
        get() = this as? Connected

    data object Disabled : WifiState(WifiStatus.Disabled)
    data object Disconnected : WifiState(WifiStatus.Disconnected)
    data class Connected(val propertyViewData: List<WifiPropertyViewData>) : WifiState(WifiStatus.Connected)
}
