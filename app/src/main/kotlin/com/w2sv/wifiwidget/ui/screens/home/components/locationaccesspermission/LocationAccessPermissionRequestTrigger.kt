package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import com.w2sv.domain.model.WidgetWifiProperty

sealed interface LocationAccessPermissionRequestTrigger {
    data object InitialAppEntry : LocationAccessPermissionRequestTrigger
    class PropertyCheckChange(val property: WidgetWifiProperty.NonIP.LocationAccessRequiring) :
        LocationAccessPermissionRequestTrigger
}
