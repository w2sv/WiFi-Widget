package com.w2sv.wifiwidget.ui

import androidx.activity.SystemBarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.navigation.dependency
import com.w2sv.composed.CollectFromFlow
import com.w2sv.domain.model.Theme
import com.w2sv.wifiwidget.ui.screens.home.components.LocationAccessPermissionStatus
import com.w2sv.wifiwidget.ui.shared_viewmodels.AppViewModel
import com.w2sv.wifiwidget.ui.shared_viewmodels.WidgetViewModel
import com.w2sv.wifiwidget.ui.states.rememberLocationAccessState
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.utils.LocalLocationManager
import com.w2sv.wifiwidget.ui.utils.LocalNavHostController
import com.w2sv.wifiwidget.ui.utils.LocalUseDarkTheme
import com.w2sv.wifiwidget.ui.utils.activityViewModel

@Composable
fun AppUI(
    setSystemBarStyles: (SystemBarStyle, SystemBarStyle) -> Unit,
    appVM: AppViewModel = activityViewModel(),
    widgetVM: WidgetViewModel = activityViewModel()
) {
    CompositionLocalProvider(
        LocalUseDarkTheme provides when (appVM.theme.collectAsStateWithLifecycle().value) {
            Theme.Light -> false
            Theme.Dark -> true
            Theme.Default -> isSystemInDarkTheme()
        }
    ) {
        AppTheme(
            useDynamicTheme = appVM.useDynamicColors.collectAsStateWithLifecycle().value,
            useAmoledBlackTheme = appVM.useAmoledBlackTheme.collectAsStateWithLifecycle().value,
            setSystemBarStyles = setSystemBarStyles
        ) {
            val navController = rememberNavController()
            val locationAccessState = rememberLocationAccessState()

            // Call configuration.onLocationAccessPermissionStatusChanged on new location access permission status
            CollectFromFlow(locationAccessState.newStatus) {
                if (it is LocationAccessPermissionStatus.Granted) {
                    widgetVM.configuration.onLocationAccessPermissionGranted(
                        it.trigger
                    )
                }
            }

            CompositionLocalProvider(
                LocalLocationManager provides appVM.locationManager,
                LocalNavHostController provides navController
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DestinationsNavHost(
                        navGraph = NavGraphs.root,
                        navController = navController,
                        startRoute = appVM.startRoute,
                        dependenciesContainerBuilder = {
                            dependency(locationAccessState)
                        }
                    )
                }
            }
        }
    }
}