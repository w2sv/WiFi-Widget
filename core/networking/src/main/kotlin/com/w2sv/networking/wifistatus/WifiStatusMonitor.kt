package com.w2sv.networking.wifistatus

import com.w2sv.domain.model.WifiStatus
import kotlinx.coroutines.flow.Flow

interface WifiStatusMonitor {
    val wifiStatus: Flow<WifiStatus>
}
