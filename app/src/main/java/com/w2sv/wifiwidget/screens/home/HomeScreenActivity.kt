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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.w2sv.wifiwidget.ApplicationActivity
import com.w2sv.wifiwidget.preferences.WidgetPreferences
import com.w2sv.wifiwidget.ui.AppTheme
import com.w2sv.wifiwidget.widget.WifiWidgetProvider

class HomeScreenActivity : ApplicationActivity() {

    class ViewModel : androidx.lifecycle.ViewModel() {
        val propertyKey2State: SnapshotStateMap<String, Boolean> = mutableStateMapOf(
            *WidgetPreferences
                .keys
                .map { it to WidgetPreferences.getValue(it) }
                .toTypedArray()
        )

        /**
         * @return flag indicating whether any property has been updated
         */
        fun syncWidgetProperties(): Boolean {
            var updatedProperty = false
            propertyKey2State.forEach { (k, v) ->
                if (v != WidgetPreferences.getValue(k)) {
                    WidgetPreferences[k] = v
                    updatedProperty = true
                }
            }

            return updatedProperty
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setExitAnimation()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                HomeScreen(
                    requestPinWidget = { requestPinWidget() },
                    launchLocationPermissionRequest = {
                        locationPermissionRequestLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    }
                )
            }
        }
    }

    private fun SplashScreen.setExitAnimation() {
        setOnExitAnimationListener { splashScreenViewProvider ->
            val splashScreenView = splashScreenViewProvider.view
            ObjectAnimator.ofFloat(
                splashScreenView,
                View.TRANSLATION_Y,
                0f,
                -splashScreenView.height.toFloat()
            ).apply {
                interpolator = AnticipateInterpolator()
                duration = 200L
                doOnEnd { splashScreenViewProvider.remove() }
                start()
            }
        }
    }

    private fun requestPinWidget() {
        getSystemService(AppWidgetManager::class.java).let {
            if (it.isRequestPinAppWidgetSupported) {
                it.requestPinAppWidget(
                    ComponentName(
                        this,
                        WifiWidgetProvider::class.java
                    ),
                    null,
                    null
                )
            }
        }
    }

    private val locationPermissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            // sync permission grant result with "showSSID" states
            it.values.contains(true).let { permissionGranted ->
                viewModel.propertyKey2State["showSSID"] = permissionGranted
                WidgetPreferences.showSSID = permissionGranted
            }

            requestPinWidget()
        }

    private val viewModel by viewModels<ViewModel>()
}