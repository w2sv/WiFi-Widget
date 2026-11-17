package com.w2sv.wifiwidget.ui.screen.locationaccess.flow

import com.w2sv.wifiwidget.ui.location.LocationPermissionLevel

/**
 * Temporary state for a pending location-permission interaction, not authoritative Android access. [displayedPermissionLevel] retains the action level
 * captured for this operation, even if Android permissions change before it completes. Settings
 * states use `hasLeftApp` to delay result reconciliation until the app has actually been left.
 */
internal sealed interface LocationAccessFlowState {
    val displayedPermissionLevel: LocationPermissionLevel

    @JvmInline
    value class RequestingForeground(override val displayedPermissionLevel: LocationPermissionLevel) : LocationAccessFlowState

    data class ForegroundSettings(override val displayedPermissionLevel: LocationPermissionLevel, val hasLeftApp: Boolean = false) :
        LocationAccessFlowState

    @JvmInline
    value class BackgroundDisclosure(override val displayedPermissionLevel: LocationPermissionLevel) : LocationAccessFlowState

    @JvmInline
    value class RequestingBackground(override val displayedPermissionLevel: LocationPermissionLevel) : LocationAccessFlowState

    data class BackgroundSettings(override val displayedPermissionLevel: LocationPermissionLevel, val hasLeftApp: Boolean = false) :
        LocationAccessFlowState

    @JvmInline
    value class Completing(override val displayedPermissionLevel: LocationPermissionLevel) : LocationAccessFlowState
}
