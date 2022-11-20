package com.w2sv.wifiwidget.screens.home

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.w2sv.wifiwidget.ApplicationActivity
import com.w2sv.wifiwidget.preferences.WidgetPreferences
import com.w2sv.wifiwidget.ui.AppTheme
import com.w2sv.wifiwidget.widget.WifiWidgetProvider

class HomeActivity : ApplicationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                HomeScreen(
                    requestPinWidget = { requestPinWidget() },
                    launchLocationPermissionRequest = {
                        locationPermissionRequestLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    }
                )
            }
        }
    }

    private fun requestPinWidget() {
        getSystemService(AppWidgetManager::class.java).let {
            if (it.isRequestPinAppWidgetSupported) {
                it.requestPinAppWidget(
                    ComponentName(
                        this,
                        WifiWidgetProvider::class.java
                    ),
                    null,
                    null
                )
            }
        }
    }

    private val locationPermissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            // sync permission grant result with "showSSID" states
            it.values.contains(true).let { permissionGranted ->
                viewModel.propertyKey2State["showSSID"] = permissionGranted
                WidgetPreferences.showSSID = permissionGranted
            }

            requestPinWidget()
        }

    private val viewModel by viewModels<HomeScreenViewModel>()
}

