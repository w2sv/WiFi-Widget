package com.w2sv.widget.model

import com.w2sv.data.networking.IPAddress

internal sealed interface WifiPropertyLayoutViewData {
    class IPProperties(val ipAddress: IPAddress): WifiPropertyLayoutViewData
    class WifiProperty(val label: String, val value: String): WifiPropertyLayoutViewData
}