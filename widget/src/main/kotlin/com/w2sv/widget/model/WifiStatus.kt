package com.w2sv.widget.model

enum class WifiStatus(val isConnected: Boolean) {
    Disabled(false),
    Disconnected(false),
    Connected(true)
}