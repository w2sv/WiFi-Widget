@file:Suppress("DEPRECATION")

package com.w2sv.wifiwidget.ui

import android.animation.ObjectAnimator
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.w2sv.androidutils.generic.getIntExtraOrNull
import com.w2sv.androidutils.lifecycle.SelfManagingLocalBroadcastReceiver
import com.w2sv.data.model.Theme
import com.w2sv.widget.WidgetDataRefreshWorker
import com.w2sv.wifiwidget.ui.screens.home.HomeScreen
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.viewmodels.HomeScreenViewModel
import com.w2sv.wifiwidget.ui.viewmodels.InAppThemeViewModel
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeScreenVM by viewModels<HomeScreenViewModel>()
    private val widgetVM by viewModels<WidgetViewModel>()
    private val inAppThemeVM by viewModels<InAppThemeViewModel>()

    @Inject
    lateinit var widgetDataRefreshWorkerManager: WidgetDataRefreshWorker.Manager

    class AppWidgetOptionsChangedReceiver(
        broadcastManager: LocalBroadcastManager,
        callback: (Context?, Intent?) -> Unit
    ) : SelfManagingLocalBroadcastReceiver.Impl(
        broadcastManager,
        IntentFilter(AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED),
        callback
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        handleSplashScreen {
            homeScreenVM.onSplashScreenAnimationFinished()
        }

        super.onCreate(savedInstanceState)

        lifecycle.addObserver(
            AppWidgetOptionsChangedReceiver(LocalBroadcastManager.getInstance(this@MainActivity)) { _, intent ->
                i { "WifiWidgetOptionsChangedReceiver.onReceive | ${intent?.extras?.keySet()}" }

                intent?.getIntExtraOrNull(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    ?.let { widgetId ->
                        homeScreenVM.onWidgetOptionsUpdated(
                            widgetId,
                            this@MainActivity
                        )
                    }
            }
        )

        lifecycleScope.subscribeToFlows()

        setContent {
            AppTheme(
                useDynamicTheme = inAppThemeVM.useDynamicTheme.collectAsState(initial = false).value,
                darkTheme = when (inAppThemeVM.inAppTheme.collectAsState(initial = Theme.SystemDefault).value) {
                    Theme.Light -> false
                    Theme.Dark -> true
                    Theme.SystemDefault -> isSystemInDarkTheme()
                    else -> throw Error()
                }
            ) {
                HomeScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        homeScreenVM.triggerWifiPropertiesViewDataRefresh()
    }

    /**
     * Sets SwipeUp exit animation and triggers call to [onAnimationFinished] in time.
     */
    private fun handleSplashScreen(onAnimationFinished: () -> Unit) {
        installSplashScreen().setOnExitAnimationListener { splashScreenViewProvider ->
            ObjectAnimator.ofFloat(
                splashScreenViewProvider.view,
                View.TRANSLATION_Y,
                0f,
                -splashScreenViewProvider.view.height.toFloat()
            )
                .apply {
                    interpolator = AnticipateInterpolator()
                    duration = 400L
                    doOnEnd {
                        splashScreenViewProvider.remove()
                        onAnimationFinished()
                    }
                }
                .start()
        }
    }

    private fun LifecycleCoroutineScope.subscribeToFlows() {
        launch {
            widgetVM.refreshingParametersChanged.collect {
                widgetDataRefreshWorkerManager
                    .applyChangedParameters()
            }
        }
        launch {
            homeScreenVM.exitApplication.collect {
                finishAffinity()
            }
        }
    }
}