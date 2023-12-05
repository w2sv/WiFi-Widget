package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import com.w2sv.domain.model.WidgetWifiProperty

sealed interface LocationAccessPermissionRequiringAction {
    data object PinWidgetButtonPress : LocationAccessPermissionRequiringAction
    class PropertyCheckChange(val property: WidgetWifiProperty.NonIP.LocationAccessRequiring) :
        LocationAccessPermissionRequiringAction
}
