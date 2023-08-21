package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import com.w2sv.data.model.WifiProperty

sealed interface LocationAccessPermissionRequiringAction {
    data object PinWidgetButtonPress : LocationAccessPermissionRequiringAction
    class PropertyCheckChange(val property: WifiProperty) : LocationAccessPermissionRequiringAction
}
