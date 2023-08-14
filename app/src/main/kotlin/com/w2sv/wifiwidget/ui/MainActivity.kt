package com.w2sv.wifiwidget.ui

import android.animation.ObjectAnimator
import android.appwidget.AppWidgetManager
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
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.coroutines.launchFlowCollections
import com.w2sv.androidutils.generic.getIntExtraOrNull
import com.w2sv.common.constants.Extra
import com.w2sv.data.model.Theme
import com.w2sv.wifiwidget.ui.screens.home.HomeScreen
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.viewmodels.HomeScreenViewModel
import com.w2sv.wifiwidget.ui.viewmodels.NavigationDrawerViewModel
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.FlowCollector
import slimber.log.i

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeScreenVM by viewModels<HomeScreenViewModel>()
    private val widgetVM by viewModels<WidgetViewModel>()
    private val navigationDrawerVM by viewModels<NavigationDrawerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        handleSplashScreen(
            onAnimationFinished = {
                if (intent.hasExtra(Extra.OPEN_WIDGET_CONFIGURATION_DIALOG)) {
                    homeScreenVM.showWidgetConfigurationDialog.value = true
                }
            }
        )

        super.onCreate(savedInstanceState)

        lifecycle.addObserver(
            AppWidgetOptionsChangedReceiver(
                context = this,
                callback = { _, intent ->
                    i { "WifiWidgetOptionsChangedReceiver.onReceive | ${intent?.extras?.keySet()}" }

                    intent
                        ?.getIntExtraOrNull(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                        ?.let { widgetId ->
                            homeScreenVM.onWidgetOptionsUpdated(
                                widgetId,
                                this
                            )
                        }
                }
            )
        )
        lifecycleScope.launchFlowCollections(
            homeScreenVM.exitApplication to FlowCollector {
                finishAffinity()
            }
        )

        setContent {
            AppTheme(
                useDynamicTheme = navigationDrawerVM.useDynamicTheme.collectAsState(initial = false).value,
                darkTheme = when (navigationDrawerVM.inAppTheme.collectAsState(initial = Theme.SystemDefault).value) {
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

    /**
     * Sets SwipeUp exit animation and calls [onAnimationFinished].
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

    override fun onStart() {
        super.onStart()

        homeScreenVM.onStart(this)
    }
}