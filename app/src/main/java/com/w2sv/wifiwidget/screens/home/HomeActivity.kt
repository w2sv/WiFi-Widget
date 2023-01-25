package com.w2sv.wifiwidget.screens.home

import android.Manifest
import android.animation.ObjectAnimator
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import com.w2sv.wifiwidget.ApplicationActivity
import com.w2sv.wifiwidget.preferences.GlobalFlags
import com.w2sv.wifiwidget.preferences.WidgetProperties
import com.w2sv.wifiwidget.ui.AppTheme
import com.w2sv.wifiwidget.utils.getMutableStateMap
import com.w2sv.wifiwidget.widget.WifiWidgetProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class HomeActivity : ApplicationActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(
        private val widgetProperties: WidgetProperties,
        private val globalFlags: GlobalFlags
    ) : androidx.lifecycle.ViewModel() {

        val widgetPropertyStates: SnapshotStateMap<String, Boolean> by lazy {
            widgetProperties.getMutableStateMap()
        }

        /**
         * @return flag indicating whether any property has been updated
         */
        fun syncWidgetProperties(): Boolean {
            var updatedAnyProperty = false

            widgetPropertyStates.forEach { (k, v) ->
                if (v != widgetProperties.getValue(k)) {
                    widgetProperties[k] = v
                    updatedAnyProperty = true
                }
            }

            return updatedAnyProperty
        }

        val locationPermissionDialogShown: Boolean by globalFlags::locationPermissionDialogShown

        fun onLocationPermissionDialogShown(){
            globalFlags.locationPermissionDialogShown = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener(
            SwipeUpAnimation()
        )

        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                HomeScreen()
            }
        }
    }

    fun launchLocationPermissionRequest(){
        locationPermissionRequestLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    fun requestWidgetPin() {
        getSystemService(AppWidgetManager::class.java).let {
            if (it.isRequestPinAppWidgetSupported) {
                it.requestPinAppWidget(
                    ComponentName(
                        this,
                        WifiWidgetProvider::class.java
                    ),
                    null,
                    null  // add toast callback
                )
            }
        }
    }

    private val locationPermissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionGrantedMap ->
            // sync permission grant result with "showSSID" states
            if (permissionGrantedMap.containsValue(true)){
                widgetProperties.showSSID = true
                viewModels<ViewModel>().value.widgetPropertyStates[widgetProperties::showSSID.name] = true
            }

            requestWidgetPin()
        }
}

private class SwipeUpAnimation: SplashScreen.OnExitAnimationListener{
    override fun onSplashScreenExit(splashScreenViewProvider: SplashScreenViewProvider) {
        val splashScreenView = splashScreenViewProvider.view
        ObjectAnimator.ofFloat(
            splashScreenView,
            View.TRANSLATION_Y,
            0f,
            -splashScreenView.height.toFloat()
        ).apply {
            interpolator = AnticipateInterpolator()
            duration = 400L
            doOnEnd { splashScreenViewProvider.remove() }
            start()
        }
    }
}