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
import androidx.compose.runtime.snapshots.SnapshotStateMap
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
import com.w2sv.preferences.GlobalFlags
import com.w2sv.preferences.IntPreferences
import com.w2sv.preferences.WidgetProperties
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.wifiwidget.ui.WifiWidgetTheme
import com.w2sv.wifiwidget.ui.home.HomeScreen
import com.w2sv.wifiwidget.utils.getMutableStateMap
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(
        private val widgetProperties: WidgetProperties,
        private val globalFlags: GlobalFlags,
        private val intPreferences: IntPreferences,
        savedStateHandle: SavedStateHandle,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val openPropertiesConfigurationDialogOnStart =
            savedStateHandle.contains(WifiWidgetProvider.EXTRA_OPEN_PROPERTIES_CONFIGURATION_DIALOG_ON_START)
                .also { i { "openPropertiesConfigurationDialog: $it" } }

        val ssidKey: String = widgetProperties::SSID.name

        /**
         * Widget Creation Listening
         */

        fun onWidgetOptionsUpdated(widgetId: Int, context: Context) {
            if (widgetIds.add(widgetId)) {
                i { "New widgetId: $widgetId" }
                context.showToast("Pinned Widget")
                if (widgetProperties.SSID && !context.locationServicesEnabled)
                    context.showToast("SSID display requires location access!", Toast.LENGTH_LONG)
            }
        }

        private val widgetIds: MutableSet<Int> =
            WifiWidgetProvider.getWidgetIds(context).toMutableSet()

        /**
         * widgetPropertyStates
         */

        val widgetPropertyFlags: SnapshotStateMap<String, Boolean> by lazy {
            widgetProperties.getMutableStateMap()
        }

        /**
         * @return Boolean indicating whether change has been endorsed
         */
        fun setWidgetPropertyFlag(property: String, value: Boolean): Boolean {
            // veto change if leading to all properties being unchecked
            if (!value && widgetPropertyFlags.values.count { true } == 1)
                return false

            // implement change and update _propertyStatesDissimilar
            widgetPropertyFlags[property] = value
            widgetPropertyFlagsRequiringUpdate.value =
                widgetPropertyFlags.any { (k, v) ->  // TODO: optimize
                    v != widgetProperties.getValue(k)
                }
            return true
        }

        fun setSSIDState(value: Boolean, updateRequiringUpdate: Boolean = true) {
            when (updateRequiringUpdate) {
                true -> setWidgetPropertyFlag(ssidKey, value)
                false -> widgetPropertyFlags[ssidKey] = value
            }
        }

        fun updateWidgetConfiguration() {
            widgetProperties.putAll(widgetPropertyFlags)
            intPreferences.widgetTheme = widgetTheme.value

            resetRequiringUpdateFlows()
        }

        fun resetWidgetConfiguration() {
            widgetPropertyFlags.putAll(widgetProperties)
            widgetTheme.value = intPreferences.widgetTheme

            resetRequiringUpdateFlows()
        }

        val widgetConfigurationRequiringUpdate = MutableStateFlow(false)

        private val widgetPropertyFlagsRequiringUpdate = MutableStateFlow(false)
        private val widgetThemeRequiringUpdate = MutableStateFlow(false)

        init {
            val updateWidgetConfigurationRequiringUpdate: (Boolean) -> Unit = {
                widgetConfigurationRequiringUpdate.value =
                    widgetThemeRequiringUpdate.value || widgetPropertyFlagsRequiringUpdate.value
            }
            viewModelScope.launch {
                widgetThemeRequiringUpdate.collect(updateWidgetConfigurationRequiringUpdate)
            }
            viewModelScope.launch {
                widgetPropertyFlagsRequiringUpdate.collect(updateWidgetConfigurationRequiringUpdate)
            }
        }

        private fun resetRequiringUpdateFlows() {
            widgetThemeRequiringUpdate.value = false
            widgetPropertyFlagsRequiringUpdate.value = false
        }

        var widgetTheme = MutableStateFlow(intPreferences.widgetTheme)
            .apply {
                viewModelScope.launch {
                    collect {
                        widgetThemeRequiringUpdate.value = it != intPreferences.widgetTheme
                    }
                }
            }

        /**
         * lap := Location Access Permission
         */

        val lapDialogAnswered: Boolean by globalFlags::locationPermissionDialogAnswered

        fun onLapDialogAnswered() {
            globalFlags.locationPermissionDialogAnswered = true
        }
    }

    @Inject
    lateinit var globalFlags: GlobalFlags

    @Inject
    lateinit var widgetProperties: WidgetProperties

    @Inject
    lateinit var intPreferences: IntPreferences

    inner class WifiWidgetOptionsChangedReceiver : SelfManagingLocalBroadcastReceiver(
        LocalBroadcastManager.getInstance(this),
        IntentFilter(AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED)
    ) {
        override fun onReceive(context: Context?, intent: Intent?) {
            i { "WifiWidgetOptionsChangedReceiver.onReceive | ${intent?.extras?.keySet()}" }

            intent?.getIntExtraOrNull(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                ?.let { widgetId ->
                    viewModels<ViewModel>().value.onWidgetOptionsUpdated(
                        widgetId,
                        this@HomeActivity
                    )
                }
        }
    }

    val lapRequestLauncher by lazy {
        LocationAccessPermissionHandler(this)
    }

    override val lifecycleObservers: List<LifecycleObserver>
        get() = listOf(
            globalFlags,
            widgetProperties,
            intPreferences,
            lapRequestLauncher,
            WifiWidgetOptionsChangedReceiver()
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener(
            SwipeUpAnimation()
        )

        super.onCreate(savedInstanceState)

        setContent {
            WifiWidgetTheme {
                HomeScreen()
            }
        }
    }
}

private class SwipeUpAnimation : SplashScreen.OnExitAnimationListener {
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
                doOnEnd { splashScreenViewProvider.remove() }
            }
            .start()
    }
}