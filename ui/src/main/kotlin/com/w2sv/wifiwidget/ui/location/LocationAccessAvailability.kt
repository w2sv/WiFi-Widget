package com.w2sv.wifiwidget.ui.location

import com.w2sv.domain.location.LocationPermissionLevel
import com.w2sv.wifiwidget.ui.location.capability.access.LocationAccessCapability

/**
 * Immutable snapshot shared by screen presentation and process-wide widget recovery. [permissionLevel] describes granted
 * access independently of the system-wide Location setting; disabling services does not revoke
 * permissions. Unlike [LocationAccessCapability], this model has no Android operations or streams.
 */
internal data class LocationAccessAvailability(val permissionLevel: LocationPermissionLevel, val locationServicesEnabled: Boolean)
