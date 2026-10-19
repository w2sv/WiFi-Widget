package com.w2sv.domain.wifiproperty.presentation

import com.w2sv.domain.wifiproperty.WifiPropertyResolutionError

data class WifiPropertyPresentation(
    val label: SubscriptableText,
    val value: String,
    val subValues: List<String> = emptyList(),
    val resolutionError: WifiPropertyResolutionError? = null
)
