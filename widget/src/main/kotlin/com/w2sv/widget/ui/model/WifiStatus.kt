package com.w2sv.widget.ui.model

enum class WifiStatus(val isConnected: Boolean) {
    Disabled(false),
    Disconnected(false),
    Connected(true)
}