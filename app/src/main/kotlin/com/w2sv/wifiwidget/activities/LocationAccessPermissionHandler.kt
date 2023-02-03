package com.w2sv.wifiwidget.activities

import android.Manifest
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.permissionhandler.CoupledPermissionsHandler

class LocationAccessPermissionHandler(activity: ComponentActivity) :
    CoupledPermissionsHandler(
        activity,
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ),
        "LocationAccessPermissionHandler"
    ) {
    override fun onPermissionRationalSuppressed() {
        activity.showToast(
            "Go to app settings and grant location access permission",
            Toast.LENGTH_LONG
        )
    }
}