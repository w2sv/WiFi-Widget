package com.w2sv.wifiwidget.ui.screen.locationaccess.state

import com.w2sv.wifiwidget.ui.location.LocationPermissionLevel
import com.w2sv.wifiwidget.ui.location.capability.access.LocationAccessCapability

/**
 * Immutable snapshot used to derive screen presentation. [permissionLevel] describes granted
 * access independently of the system-wide Location setting; disabling services does not revoke
 * permissions. Unlike [LocationAccessCapability], this model has no Android operations or streams.
 */
internal data class LocationAccessAvailability(val permissionLevel: LocationPermissionLevel, val locationServicesEnabled: Boolean)
