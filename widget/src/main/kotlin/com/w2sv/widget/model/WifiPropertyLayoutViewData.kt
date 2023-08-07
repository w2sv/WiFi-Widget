package com.w2sv.widget.model

import com.w2sv.data.networking.IPAddress

internal sealed interface WifiPropertyLayoutViewData {
    class IPProperties(
        val ipAddress: IPAddress,
        val showPrefixLength: Boolean,
        val showAdditionalProperties: Boolean
    ) : WifiPropertyLayoutViewData

    class WifiProperty(val label: CharSequence, val value: CharSequence) : WifiPropertyLayoutViewData
}