package com.w2sv.wifiwidget.ui.screen.home.model.gpsstatus

import kotlinx.coroutines.flow.Flow

/**
 * Provides a [Flow] indicating whether location (GPS) is enabled on the device.
 * Emits only when the enabled state changes.
 */
interface LocationEnabledProvider {

    /**
     * Emits only on state change.
     */
    val isEnabled: Flow<Boolean>
}
