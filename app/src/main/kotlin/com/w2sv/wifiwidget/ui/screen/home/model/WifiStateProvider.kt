package com.w2sv.wifiwidget.ui.screen.home.model

import kotlinx.coroutines.flow.StateFlow

interface WifiStateProvider {
    val wifiState: StateFlow<WifiState>
    fun onLocationAccessChanged()
    fun refreshProperties()
}
