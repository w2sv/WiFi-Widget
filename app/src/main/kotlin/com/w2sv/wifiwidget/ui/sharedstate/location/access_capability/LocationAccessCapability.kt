package com.w2sv.wifiwidget.ui.sharedstate.location.access_capability

import com.w2sv.wifiwidget.ui.sharedstate.location.permission_capability.LocationPermissionCapability

interface LocationAccessCapability : LocationPermissionCapability {
    val isGpsEnabled: Boolean
}
