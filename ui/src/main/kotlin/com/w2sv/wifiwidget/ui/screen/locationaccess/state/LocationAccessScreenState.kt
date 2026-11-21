package com.w2sv.wifiwidget.ui.screen.locationaccess.state

import com.w2sv.wifiwidget.ui.location.LocationAccessAvailability
import com.w2sv.wifiwidget.ui.location.LocationPermissionLevel

/**
 * Inputs for Location Access presentation. [LocationAccessAvailability.permissionLevel] reflects
 * actual Android permission availability, while
 * [displayedPermissionLevel] holds the action level captured for an ongoing interaction so button
 * labels do not change before that interaction finishes. [foregroundRequestOpensSettings] is the
 * resolved foreground-action destination: approximate-only access or a suppressed runtime request
 * opens Settings, and an active foreground operation retains the destination it started with.
 * [actionsEnabled] indicates whether the flow accepts actions. This is a presentation input,
 * not the platform capability or the permission-flow state machine.
 */
internal data class LocationAccessScreenState(
    val isOnboarding: Boolean,
    val availability: LocationAccessAvailability,
    val displayedPermissionLevel: LocationPermissionLevel,
    val foregroundRequestOpensSettings: Boolean,
    val actionsEnabled: Boolean
)
