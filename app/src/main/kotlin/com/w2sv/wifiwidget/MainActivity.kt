package com.w2sv.wifiwidget

import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.w2sv.androidutils.location.isLocationEnabledCompat
import com.w2sv.androidutils.service.systemService
import com.w2sv.common.AppAction
import com.w2sv.wifiwidget.ui.AppUI
import com.w2sv.wifiwidget.ui.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val locationManager = systemService<LocationManager>()

        setContent {
            AppUI(
                initialScreen = remember(intent) { intent.initialScreen() },
                isGpsEnabled = { locationManager.isLocationEnabledCompat() },
                setSystemBarStyles = { statusBarStyle, navigationBarStyle ->
                    enableEdgeToEdge(statusBarStyle, navigationBarStyle)
                }
            )
        }
    }
}

private fun Intent.initialScreen(): Screen =
    if (action == AppAction.OPEN_WIDGET_CONFIGURATION_SCREEN) {
        Screen.WidgetConfiguration
    } else {
        Screen.Home
    }
