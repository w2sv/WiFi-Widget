package com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.model

import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.model.WifiStatus
import kotlinx.coroutines.flow.Flow

data class WifiState(
    val status: WifiStatus,
    val propertyViewData: Flow<WidgetWifiProperty.ValueViewData>?
)