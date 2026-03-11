package com.w2sv.wifiwidget.ui.screen.home.model.gpsstatus

import kotlinx.coroutines.flow.Flow

interface GpsStatusProvider {
    val isEnabled: Flow<Boolean>
}
