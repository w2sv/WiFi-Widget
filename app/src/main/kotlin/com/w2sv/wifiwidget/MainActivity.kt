package com.w2sv.wifiwidget

import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.domain.model.Theme
import com.w2sv.wifiwidget.ui.designsystem.LocalLocationManager
import com.w2sv.wifiwidget.ui.screens.home.HomeScreen
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.viewmodels.AppViewModel
import com.w2sv.wifiwidget.ui.viewmodels.HomeScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeScreenVM by viewModels<HomeScreenViewModel>()
    private val appVM by viewModels<AppViewModel>()

    @Inject
    lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val useDarkTheme = when (appVM.theme.collectAsStateWithLifecycle().value) {
                Theme.Light -> false
                Theme.Dark -> true
                Theme.SystemDefault -> isSystemInDarkTheme()
            }

            AppTheme(
                useDynamicTheme = appVM.useDynamicColors.collectAsStateWithLifecycle().value,
                useDarkTheme = useDarkTheme,
            ) {
                LaunchedEffect(useDarkTheme) {
                    val systemBarStyle = if (useDarkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }

                    enableEdgeToEdge(
                        statusBarStyle = systemBarStyle,
                        navigationBarStyle = systemBarStyle,
                    )
                }

                CompositionLocalProvider(LocalLocationManager provides locationManager) {
                    HomeScreen()
                }
            }
        }

        lifecycleScope.collectFromFlow(appVM.exitApplication) {
            finishAffinity()
        }
    }

    override fun onStart() {
        super.onStart()

        homeScreenVM.onStart()
    }
}