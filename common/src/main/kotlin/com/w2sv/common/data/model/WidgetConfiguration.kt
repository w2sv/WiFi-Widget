package com.w2sv.common.data.model

data class WidgetConfiguration(
    val theme: WidgetAppearance,
    val refreshing: WidgetRefreshing,
    val wifiProperties: List<WifiProperty>
)