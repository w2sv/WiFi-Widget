package com.w2sv.wifiwidget.ui.screen.locationaccess.flow

import com.w2sv.common.android.location.backgroundLocationRequiresSettings
import com.w2sv.domain.location.LocationPermissionLevel

/** An occurrence that may advance the current [LocationAccessFlowState]. Stale results are ignored. */
internal sealed interface LocationAccessEvent {
    data class RequestForeground(val opensSettings: Boolean, val displayedPermissionLevel: LocationPermissionLevel) : LocationAccessEvent

    /** Precise foreground access is now available; [permissionLevel] includes its effective background requirement. */
    @JvmInline
    value class ForegroundAccessAvailable(val permissionLevel: LocationPermissionLevel) : LocationAccessEvent

    data object ForegroundDenied : LocationAccessEvent

    @JvmInline
    value class ShowBackgroundDisclosure(val displayedPermissionLevel: LocationPermissionLevel) : LocationAccessEvent

    data object RequestBackground : LocationAccessEvent {
        val opensSettings: Boolean
            get() = backgroundLocationRequiresSettings
    }

    data object DismissBackgroundDisclosure : LocationAccessEvent

    /**
     * The pending background-access requirement is satisfied. This need not be a new permission
     * grant: older Android versions provide effective background access with precise foreground
     * permission.
     */
    data object BackgroundAccessAvailable : LocationAccessEvent

    data object BackgroundDenied : LocationAccessEvent

    data object LeavingForSettings : LocationAccessEvent

    @JvmInline
    value class Complete(val displayedPermissionLevel: LocationPermissionLevel) : LocationAccessEvent
}
