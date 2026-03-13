package com.w2sv.networking.propertyviewdata

import com.w2sv.common.Text
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyResolutionError

internal data class WifiPropertyValue(
    val value: Text,
    val subValues: List<String> = emptyList(),
    val resolutionError: WifiPropertyResolutionError? = null
)
