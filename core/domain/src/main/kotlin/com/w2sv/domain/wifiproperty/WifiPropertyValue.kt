package com.w2sv.domain.wifiproperty

import com.w2sv.common.Text

data class WifiPropertyValue(
    val value: Text,
    val subValues: List<String> = emptyList(),
    val resolutionError: WifiPropertyResolutionError? = null
)
