package com.w2sv.wifiwidget.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.w2sv.domain.location.LocationAccessRepository
import com.w2sv.wifiwidget.BuildConfig
import com.w2sv.wifiwidget.ui.location.LocationAccessDependencies
import com.w2sv.wifiwidget.ui.navigation.NavGraph
import com.w2sv.wifiwidget.ui.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var locationAccessDependencies: LocationAccessDependencies

    @Inject
    lateinit var locationAccessRepository: LocationAccessRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        var initialScreen by mutableStateOf<Screen?>(null)

        installSplashScreen().setKeepOnScreenCondition { initialScreen == null }
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)

        lifecycleScope.launch {
            val onboardingCompleted = locationAccessRepository.onboardingCompleted.first()
            initialScreen = if (onboardingCompleted) Screen.Home else Screen.LocationAccessOnboarding
        }

        setContent {
            WifiWidgetUI(
                versionName = BuildConfig.VERSION_NAME,
                locationAccessDependencies = locationAccessDependencies
            ) { themeController ->
                initialScreen?.let { screen ->
                    NavGraph(
                        themeController = themeController,
                        initialScreen = screen
                    )
                }
            }
        }
    }
}
