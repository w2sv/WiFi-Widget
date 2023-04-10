@file:Suppress("DEPRECATION")

package com.w2sv.wifiwidget.ui.screens.home

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
import androidx.compose.runtime.getValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.w2sv.androidutils.SelfManagingLocalBroadcastReceiver
import com.w2sv.androidutils.extensions.addObservers
import com.w2sv.androidutils.extensions.getIntExtraOrNull
import com.w2sv.common.Theme
import com.w2sv.widget.WidgetDataRefreshWorker
import com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.WidgetConfigurationViewModel
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import slimber.log.i

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    private val homeScreenViewModel by viewModels<HomeScreenViewModel>()
    private val widgetConfigurationViewModel by viewModels<WidgetConfigurationViewModel>()

    class AppWidgetOptionsChangedReceiver(
        broadcastManager: LocalBroadcastManager,
        callback: (Context?, Intent?) -> Unit
    ) : SelfManagingLocalBroadcastReceiver.Impl(
        broadcastManager,
        IntentFilter(AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED),
        callback
    )

    val lapRequestLauncher by lazy {
        LocationAccessPermissionHandler(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        handleSplashScreen {
            widgetConfigurationViewModel.onSplashScreenAnimationFinished()
        }

        super.onCreate(savedInstanceState)

        addObservers(
            buildList {
                add(lapRequestLauncher)
                add(
                    AppWidgetOptionsChangedReceiver(LocalBroadcastManager.getInstance(this@HomeActivity)) { _, intent ->
                        i { "WifiWidgetOptionsChangedReceiver.onReceive | ${intent?.extras?.keySet()}" }

                        intent?.getIntExtraOrNull(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                            ?.let { widgetId ->
                                homeScreenViewModel.onWidgetOptionsUpdated(
                                    widgetId,
                                    this@HomeActivity
                                )
                            }
                    }
                )
            }
        )

        lifecycleScope.subscribeToFlows()

        setContent {
            val theme by homeScreenViewModel.dataStoreRepository.inAppTheme.collectAsState(Theme.DeviceDefault)

            WifiWidgetTheme(
                darkTheme = when (theme) {
                    Theme.Light -> false
                    Theme.Dark -> true
                    Theme.DeviceDefault -> isSystemInDarkTheme()
                    else -> throw Error()
                }
            ) {
                HomeScreen()
            }
        }
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
            with(widgetConfigurationViewModel.widgetRefreshingParametersChanged) {
                collect {
                    if (it) {
                        WidgetDataRefreshWorker
                            .Administrator
                            .getInstance(applicationContext)
                            .applyChangedParameters()
                        value = false
                    }
                }
            }
        }
        launch {
            homeScreenViewModel.exitApplication.collect {
                if (it) {
                    finishAffinity()
                }
            }
        }
    }
}