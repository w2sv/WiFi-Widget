package com.w2sv.wifiwidget.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.w2sv.wifiwidget.BuildConfig
import com.w2sv.wifiwidget.ui.location.LocationAccessDependencies
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessRoute
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessScreenScenario
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Hosts the Location Access screen when launched from a home screen widget. */
@AndroidEntryPoint
class LocationAccessActivity : ComponentActivity() {
    @Inject
    lateinit var locationAccessDependencies: LocationAccessDependencies

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        onBackPressedDispatcher.addCallback(this) { finishAndRemoveTask() }

        setContent {
            WifiWidgetUI(
                versionName = BuildConfig.VERSION_NAME,
                locationAccessDependencies = locationAccessDependencies
            ) {
                LocationAccessRoute(
                    scenario = LocationAccessScreenScenario.ManageAccess,
                    onContinue = ::finishAndRemoveTask,
                    onBack = ::finishAndRemoveTask
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // A repeated widget click brings this task forward without restarting a pending request.
        setIntent(intent)
    }
}
