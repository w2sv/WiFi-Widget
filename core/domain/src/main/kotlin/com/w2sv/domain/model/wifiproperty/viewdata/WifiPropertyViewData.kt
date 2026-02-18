package com.w2sv.domain.model.wifiproperty.viewdata

data class WifiPropertyViewData(
    val label: SubscriptableText,
    val value: String,
    val subValues: List<String> = emptyList()
)

