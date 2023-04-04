package com.w2sv.wifiwidget.ui.screens.home

import android.Manifest
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.androidutils.permissionhandler.CoupledPermissionsHandler

class LocationAccessPermissionHandler(activity: ComponentActivity) :
    CoupledPermissionsHandler(
        activity,
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ),
        "LocationAccessPermissionHandler"
    ) {

    fun requestPermissionAndSetSSIDFlagCorrespondingly(
        viewModel: HomeActivity.ViewModel,
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null,
        onRequestDismissed: (() -> Unit)? = null
    ): Boolean =
        super.requestPermissionIfRequired(
            {
                viewModel.widgetPropertyStateMap["SSID"] = true
                onGranted?.invoke()
            },
            {
                viewModel.widgetPropertyStateMap["SSID"] = false
                onDenied?.invoke()
            },
            onRequestDismissed
        )

    override fun onPermissionRationalSuppressed() {
        activity.showToast(
            "Go to app settings and grant location access permission",
            Toast.LENGTH_LONG
        )
    }
}