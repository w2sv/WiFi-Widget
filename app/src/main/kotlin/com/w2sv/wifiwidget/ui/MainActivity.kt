package com.w2sv.wifiwidget.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.domain.model.Theme
import com.w2sv.wifiwidget.ui.screens.home.HomeScreen
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.viewmodels.AppViewModel
import com.w2sv.wifiwidget.ui.viewmodels.HomeScreenViewModel
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeScreenVM by viewModels<HomeScreenViewModel>()
    private val widgetVM by viewModels<WidgetViewModel>()
    private val appVM by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        collectFromFlows()

        setContent {
            AppTheme(
                useDynamicTheme = appVM.useDynamicColors.collectAsStateWithLifecycle().value,
                darkTheme = when (appVM.theme.collectAsStateWithLifecycle().value) {
                    Theme.Light -> false
                    Theme.Dark -> true
                    Theme.SystemDefault -> isSystemInDarkTheme()
                    else -> throw Error()
                },
            ) {
                HomeScreen()
            }
        }
    }

    private fun collectFromFlows() {
        with(lifecycleScope) {
            collectFromFlow(appVM.exitApplication) {
                finishAffinity()
            }
            collectFromFlow(widgetVM.launchBackgroundLocationAccessPermissionRequest) {
                homeScreenVM.lapState.backgroundAccessState?.launchRequest()
            }
            collectFromFlow(homeScreenVM.lapState.grantTrigger) {
                widgetVM.configuration.onLocationAccessPermissionStatusChanged(true, it)
            }
            collectFromFlow(homeScreenVM.lapState.isGranted) {
                widgetVM.configuration.onLocationAccessPermissionStatusChanged(it, null)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        homeScreenVM.onStart()
        widgetVM.onStart()
    }
}
