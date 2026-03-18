package com.w2sv.wifiwidget.ui.screen.home.model.wifistate

import androidx.compose.runtime.Immutable
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewData

@Immutable
sealed class WifiState(open val status: WifiStatus) {
    val asConnectedOrNull
        get() = this as? Connected

    data object Disabled : WifiState(WifiStatus.Disabled)
    data object Disconnected : WifiState(WifiStatus.NotConnected)
    data class Connected(override val status: WifiStatus, val propertyViewData: List<WifiPropertyViewData>) : WifiState(status)
}
