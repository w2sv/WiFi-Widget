package com.w2sv.domain.model

sealed interface WidgetWifiState {

    data object Disabled : WidgetWifiState
    data object Disconnected : WidgetWifiState
    sealed interface Connected : WidgetWifiState {
        data object PropertiesLoading : Connected

        @JvmInline
        value class PropertiesAvailable(val properties: List<WifiProperty.ViewData>) : Connected
    }
}
