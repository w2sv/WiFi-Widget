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
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.navigation.dependency
import com.w2sv.domain.model.Theme
import com.w2sv.wifiwidget.ui.LocalNavHostController
import com.w2sv.wifiwidget.ui.activityViewModel
import com.w2sv.wifiwidget.ui.designsystem.LocalLocationManager
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.rememberLocationAccessState
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.utils.CollectFromFlow
import com.w2sv.wifiwidget.ui.viewmodels.AppViewModel
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
                // Reset system bar styles on theme change
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

                val navController = rememberNavController()
                val locationAccessState = rememberLocationAccessState()

                CompositionLocalProvider(
                    LocalLocationManager provides locationManager,
                    LocalNavHostController provides navController
                ) {
                    DestinationsNavHost(
                        navGraph = NavGraphs.root,
                        navController = navController,
                        dependenciesContainerBuilder = {
                            val widgetVM = activityViewModel<WidgetViewModel>()

                            // Call configuration.onLocationAccessPermissionStatusChanged on new location access permission status
                            CollectFromFlow(locationAccessState.newStatus) {
                                widgetVM.configuration.onLocationAccessPermissionStatusChanged(
                                    it
                                )
                            }

                            dependency(locationAccessState)
                        }
                    )
                }
            }
        }
    }
}