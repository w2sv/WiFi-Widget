package com.w2sv.networking.propertypresentation

import com.w2sv.common.Text
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyResolutionError

internal data class WifiPropertyValue(
    val value: Text,
    val subValues: List<String> = emptyList(),
    val resolutionError: WifiPropertyResolutionError? = null
)
