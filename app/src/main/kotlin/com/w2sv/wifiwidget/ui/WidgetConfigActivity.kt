package com.w2sv.wifiwidget.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.w2sv.wifiwidget.ui.location.LocationAccessDependencies
import com.w2sv.wifiwidget.ui.screen.widgetconfig.WidgetConfigRoute
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    @Inject
    lateinit var locationAccessDependencies: LocationAccessDependencies

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)

        setContent {
            WifiWidgetUI(locationAccessDependencies = locationAccessDependencies) {
                WidgetConfigRoute(navigateBack = ::finish)
            }
        }
    }
}
