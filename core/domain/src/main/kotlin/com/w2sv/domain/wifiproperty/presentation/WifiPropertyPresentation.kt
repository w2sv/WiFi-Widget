package com.w2sv.domain.wifiproperty.presentation

data class WifiPropertyPresentation(
    val label: SubscriptableText,
    val value: String,
    val subValues: List<String> = emptyList(),
    val resolutionError: WifiPropertyResolutionError? = null
)
