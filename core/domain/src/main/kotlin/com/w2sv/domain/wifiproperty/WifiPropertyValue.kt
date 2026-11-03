package com.w2sv.domain.wifiproperty

data class WifiPropertyValue(
    val value: Text,
    val subValues: List<String> = emptyList(),
    val resolutionError: WifiPropertyResolutionError? = null
)
