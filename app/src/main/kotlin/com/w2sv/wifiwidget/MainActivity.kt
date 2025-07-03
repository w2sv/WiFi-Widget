package com.w2sv.wifiwidget

import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.w2sv.common.AppExtra
import com.w2sv.wifiwidget.ui.AppUI
import com.w2sv.wifiwidget.ui.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AppUI(
                initialScreen = remember(intent) { intent.initialScreen() },
                locationManager = locationManager,
                setSystemBarStyles = remember {
                    { statusBarStyle, navigationBarStyle ->
                        enableEdgeToEdge(statusBarStyle, navigationBarStyle)
                    }
                }
            )
        }
    }
}

private fun Intent.initialScreen(): Screen =
    if (getBooleanExtra(AppExtra.INVOKE_WIDGET_CONFIGURATION_SCREEN, false)) {
        Screen.WidgetConfiguration
    } else {
        Screen.Home
    }
