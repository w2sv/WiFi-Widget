@file:Suppress("DEPRECATION")

package com.w2sv.wifiwidget.activities

import android.animation.ObjectAnimator
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.w2sv.androidutils.SelfManagingLocalBroadcastReceiver
import com.w2sv.androidutils.extensions.getIntExtraOrNull
import com.w2sv.androidutils.extensions.locationServicesEnabled
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.common.Theme
import com.w2sv.kotlinutils.extensions.getByOrdinal
import com.w2sv.preferences.FloatPreferences
import com.w2sv.preferences.GlobalFlags
import com.w2sv.preferences.IntPreferences
import com.w2sv.preferences.WidgetProperties
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.WifiWidgetTheme
import com.w2sv.wifiwidget.ui.home.HomeScreen
import com.w2sv.wifiwidget.utils.CoherentNonAppliedStates
import com.w2sv.wifiwidget.utils.NonAppliedSnapshotStateMap
import com.w2sv.wifiwidget.utils.NonAppliedStateFlow
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(
        private val widgetProperties: WidgetProperties,
        private val globalFlags: GlobalFlags,
        private val intPreferences: IntPreferences,
        private val floatPreferences: FloatPreferences,
        savedStateHandle: SavedStateHandle,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val lifecycleObservers: List<LifecycleObserver>
            get() = listOf(
                widgetProperties,
                globalFlags,
                intPreferences
            )

        /**
         * onSplashScreenAnimationFinished
         */

        fun onSplashScreenAnimationFinished(){
            if (openConfigurationDialogOnSplashScreenAnimationFinished){
                openConfigurationDialog.value = true
            }
        }

        private val openConfigurationDialogOnSplashScreenAnimationFinished =
            savedStateHandle.contains(WifiWidgetProvider.EXTRA_OPEN_CONFIGURATION_DIALOG_ON_START)

        /**
         * In-App Theme
         */

        val inAppThemeState = NonAppliedStateFlow(
            viewModelScope,
            { intPreferences.inAppTheme },
            {
                intPreferences.inAppTheme = it
                appliedInAppTheme.value = getByOrdinal(it)
            }
        )

        var appliedInAppTheme = MutableStateFlow<Theme>(getByOrdinal(intPreferences.inAppTheme))

        /**
         * Widget Pin Listening
         */

        fun onWidgetOptionsUpdated(widgetId: Int, context: Context) {
            if (widgetIds.add(widgetId)) {
                onNewWidgetPinned(widgetId, context)
            }
        }

        private fun onNewWidgetPinned(widgetId: Int, context: Context) {
            i { "Pinned new widget w ID=$widgetId" }
            context.showToast(R.string.pinned_widget)

            if (widgetProperties.SSID && !context.locationServicesEnabled)
                context.showToast(
                    R.string.ssid_display_requires_location_services_to_be_enabled,
                    Toast.LENGTH_LONG
                )
        }

        private val widgetIds: MutableSet<Int> =
            WifiWidgetProvider.getWidgetIds(context).toMutableSet()

        /**
         * Widget Configuration
         */

        val openConfigurationDialog = MutableStateFlow(false)

        val widgetPropertyStateMap = NonAppliedSnapshotStateMap(
            { widgetProperties },
            { widgetProperties.putAll(it) }
        )

        val widgetThemeState = NonAppliedStateFlow(
            viewModelScope,
            { intPreferences.widgetTheme },
            { intPreferences.widgetTheme = it }
        )

        val widgetOpacityState = NonAppliedStateFlow(
            viewModelScope,
            { floatPreferences.opacity },
            { floatPreferences.opacity = it }
        )

        val widgetConfigurationStates = CoherentNonAppliedStates(
            widgetPropertyStateMap,
            widgetThemeState,
            widgetOpacityState,
            coroutineScope = viewModelScope
        )

        /**
         * @return Boolean indicating whether change has been confirmed
         */
        fun onUnconfirmedWidgetPropertyChange(property: String, value: Boolean): Boolean =
            (value || widgetPropertyStateMap.values.count { true } != 1).let { changeConfirmed ->
                if (changeConfirmed) {
                    widgetPropertyStateMap[property] = value
                }
                changeConfirmed
            }

        /**
         * lap := Location Access Permission
         */

        var lapDialogAnswered: Boolean by globalFlags::locationPermissionDialogAnswered
    }

    private val viewModel by viewModels<ViewModel>()

    inner class WifiWidgetOptionsChangedReceiver : SelfManagingLocalBroadcastReceiver(
        LocalBroadcastManager.getInstance(this),
        IntentFilter(AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED)
    ) {
        override fun onReceive(context: Context?, intent: Intent?) {
            i { "WifiWidgetOptionsChangedReceiver.onReceive | ${intent?.extras?.keySet()}" }

            intent?.getIntExtraOrNull(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                ?.let { widgetId ->
                    viewModel.onWidgetOptionsUpdated(
                        widgetId,
                        this@HomeActivity
                    )
                }
        }
    }

    val lapRequestLauncher by lazy {
        LocationAccessPermissionHandler(
            this,
            viewModel
        )
    }

    override val lifecycleObservers: List<LifecycleObserver>
        get() = viewModel.lifecycleObservers + listOf(
            lapRequestLauncher,
            WifiWidgetOptionsChangedReceiver()
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener(
            SwipeUpAnimation {
                viewModel.onSplashScreenAnimationFinished()
            }
        )

        super.onCreate(savedInstanceState)

        setContent {
            val theme by viewModel.appliedInAppTheme.collectAsState()

            WifiWidgetTheme(
                darkTheme = when (theme) {
                    Theme.Light -> false
                    Theme.Dark -> true
                    Theme.SystemDefault -> isSystemInDarkTheme()
                }
            ) {
                HomeScreen()
            }
        }
    }
}

private class SwipeUpAnimation(private val onEnd: () -> Unit) :
    SplashScreen.OnExitAnimationListener {
    override fun onSplashScreenExit(splashScreenViewProvider: SplashScreenViewProvider) {
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
                    onEnd()
                }
            }
            .start()
    }
}