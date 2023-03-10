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

        private val openConfigurationDialogOnStart =
            savedStateHandle.contains(WifiWidgetProvider.EXTRA_OPEN_CONFIGURATION_DIALOG_ON_START)

        var openConfigurationDialog = MutableStateFlow(false)

        var splashScreenAnimationFinished = MutableStateFlow(false)
            .apply {
                viewModelScope.launch {
                    collect {
                        if (it && openConfigurationDialogOnStart)
                            openConfigurationDialog.value = true
                    }
                }
            }

        var themeRequiringUpdate = MutableStateFlow(false)

        var theme = MutableStateFlow(intPreferences.theme)
            .apply {
                viewModelScope.launch {
                    collect {
                        themeRequiringUpdate.value = it != intPreferences.theme
                    }
                }
            }
        var usedTheme = MutableStateFlow<Theme>(getByOrdinal(intPreferences.theme))

        fun updateTheme() {
            intPreferences.theme = theme.value
            usedTheme.value = getByOrdinal(theme.value)
            themeRequiringUpdate.value = false
        }

        fun resetTheme() {
            theme.value = intPreferences.theme
            themeRequiringUpdate.value = false
        }

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

        val widgetPropertyFlags: SnapshotStateMap<String, Boolean> by lazy {
            widgetProperties.getMutableStateMap()
        }

        /**
         * @return Boolean indicating whether change has been endorsed
         */
        fun onWidgetPropertyFlagInput(property: String, value: Boolean): Boolean =
            (!value && widgetPropertyFlags.values.count { true } == 1).let { leadsToLastRemainingPropertySetToFalse ->
                if (!leadsToLastRemainingPropertySetToFalse) {
                    widgetPropertyFlags[property] = value
                    updateWidgetPropertyFlagsRequiringUpdate()
                }
                !leadsToLastRemainingPropertySetToFalse
            }

        private fun updateWidgetPropertyFlagsRequiringUpdate() {
            widgetPropertyFlagsRequiringUpdate.value =
                widgetPropertyFlags.any { (k, v) ->  // TODO: optimize
                    v != widgetProperties.getValue(k)
                }
        }

        fun changeSSIDFlag(value: Boolean, updateRequiringUpdateFlow: Boolean) {
            widgetPropertyFlags[ssidKey] = value

            if (updateRequiringUpdateFlow)
                updateWidgetPropertyFlagsRequiringUpdate()
        }

        val ssidKey: String = widgetProperties::SSID.name

        fun updateWidgetConfiguration() {
            widgetProperties.putAll(widgetPropertyFlags)
            intPreferences.widgetTheme = widgetTheme.value
            floatPreferences.opacity = widgetOpacity.value

            resetRequiringUpdateFlows()
        }

        fun resetWidgetConfiguration() {
            widgetPropertyFlags.putAll(widgetProperties)
            widgetTheme.value = intPreferences.widgetTheme
            widgetOpacity.value = floatPreferences.opacity

            resetRequiringUpdateFlows()
        }

        val widgetConfigurationRequiringUpdate = MutableStateFlow(false)

        private val widgetPropertyFlagsRequiringUpdate = MutableStateFlow(false)
        private val widgetThemeRequiringUpdate = MutableStateFlow(false)
        private val widgetOpacityRequiringUpdate = MutableStateFlow(false)

        init {
            val updateWidgetConfigurationRequiringUpdate: (Boolean) -> Unit = {
                widgetConfigurationRequiringUpdate.value =
                    widgetThemeRequiringUpdate.value || widgetPropertyFlagsRequiringUpdate.value || widgetOpacityRequiringUpdate.value
            }
            with(viewModelScope) {
                launch {
                    widgetThemeRequiringUpdate.collect(updateWidgetConfigurationRequiringUpdate)
                }
                launch {
                    widgetPropertyFlagsRequiringUpdate.collect(
                        updateWidgetConfigurationRequiringUpdate
                    )
                }
                launch {
                    widgetOpacityRequiringUpdate.collect(updateWidgetConfigurationRequiringUpdate)
                }
            }
        }

        private fun resetRequiringUpdateFlows() {
            widgetThemeRequiringUpdate.value = false
            widgetPropertyFlagsRequiringUpdate.value = false
            widgetOpacityRequiringUpdate.value = false
        }

        var widgetTheme = MutableStateFlow(intPreferences.widgetTheme)
            .apply {
                viewModelScope.launch {
                    collect {
                        widgetThemeRequiringUpdate.value = it != intPreferences.widgetTheme
                    }
                }
            }

        var widgetOpacity = MutableStateFlow(floatPreferences.opacity)
            .apply {
                viewModelScope.launch {
                    collect {
                        widgetOpacityRequiringUpdate.value = it != floatPreferences.opacity
                    }
                }
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
                viewModel.splashScreenAnimationFinished.value = true
            }
        )

        super.onCreate(savedInstanceState)

        setContent {
            val theme by viewModel.usedTheme.collectAsState()

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