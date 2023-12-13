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
import com.w2sv.common.constants.Extra
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.ui.screens.home.HomeScreen
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequestTrigger
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

        if (intent.hasExtra(Extra.OPEN_WIDGET_CONFIGURATION_DIALOG)) {
            homeScreenVM.setShowWidgetConfigurationDialog(true)
        }

        lifecycleScope.collectFromFlow(appVM.exitApplication) {
            finishAffinity()
        }
        lifecycleScope.collectFromFlow(widgetVM.launchBackgroundLocationAccessPermissionRequest) {
            homeScreenVM.lapState.launchBackgroundAccessPermissionRequest()
        }
        lifecycleScope.collectFromFlow(homeScreenVM.lapState.grantInducingTrigger) { trigger ->
            when (trigger) {
                is LocationAccessPermissionRequestTrigger.InitialAppEntry -> {
                    WidgetWifiProperty.NonIP.LocationAccessRequiring.entries.forEach { property ->
                        widgetVM.configuration.wifiProperties[property] = true
                    }
                    widgetVM.configuration.launchSync()
                }
                is LocationAccessPermissionRequestTrigger.PropertyCheckChange -> {
                    widgetVM.configuration.wifiProperties[trigger.property] = true
                }
            }
        }

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

    override fun onStart() {
        super.onStart()

        homeScreenVM.onStart()
        widgetVM.onStart()
    }
}
