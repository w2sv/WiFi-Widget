package com.w2sv.wifiwidget.screens.home

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.w2sv.androidutils.ActivityCallContractHandler

class LocationAccessPermissionHandler(
    activity: ComponentActivity,
    override val resultCallback: (Map<String, Boolean>) -> Unit
) :
    ActivityCallContractHandler.Impl<Array<String>, Map<String, Boolean>>(
        activity,
        ActivityResultContracts.RequestMultiplePermissions()
    ) {

    fun launch() {
        resultLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }
}