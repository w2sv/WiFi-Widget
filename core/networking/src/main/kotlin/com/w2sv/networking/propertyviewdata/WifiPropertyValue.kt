package com.w2sv.networking.propertyviewdata

import com.w2sv.common.Text

internal data class WifiPropertyValue(val value: Text, val subValues: List<String> = emptyList(), val isError: Boolean = false)
