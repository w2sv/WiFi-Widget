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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.w2sv.androidutils.SelfManagingLocalBroadcastReceiver
import com.w2sv.androidutils.extensions.getIntExtraOrNull
import com.w2sv.androidutils.extensions.launchDelayed
import com.w2sv.androidutils.extensions.locationServicesEnabled
import com.w2sv.androidutils.extensions.reset
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.common.CustomizableWidgetSection
import com.w2sv.common.Theme
import com.w2sv.common.WifiProperty
import com.w2sv.common.preferences.CustomWidgetColors
import com.w2sv.common.preferences.EnumOrdinals
import com.w2sv.common.preferences.FloatPreferences
import com.w2sv.common.preferences.GlobalFlags
import com.w2sv.common.preferences.WidgetProperties
import com.w2sv.common.preferences.WidgetRefreshingParameters
import com.w2sv.kotlinutils.extensions.getByOrdinal
import com.w2sv.widget.WidgetDataRefreshWorker
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.CoherentNonAppliedStates
import com.w2sv.wifiwidget.ui.NonAppliedSnapshotStateMap
import com.w2sv.wifiwidget.ui.NonAppliedStateFlow
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(
        private val widgetProperties: WidgetProperties,
        private val globalFlags: GlobalFlags,
        private val enumOrdinals: EnumOrdinals,
        private val floatPreferences: FloatPreferences,
        private val widgetRefreshingParameters: WidgetRefreshingParameters,
        private val customWidgetColors: CustomWidgetColors,
        savedStateHandle: SavedStateHandle,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val lifecycleObservers: List<LifecycleObserver>
            get() = listOf(
                widgetProperties,
                globalFlags,
                enumOrdinals,
                floatPreferences,
                widgetRefreshingParameters,
                customWidgetColors
            )

        /**
         * onSplashScreenAnimationFinished
         */

        fun onSplashScreenAnimationFinished() {
            if (openConfigurationDialogOnSplashScreenAnimationFinished) {
                showWidgetConfigurationDialog.value = true
            }
        }

        private val openConfigurationDialogOnSplashScreenAnimationFinished =
            savedStateHandle.contains(WifiWidgetProvider.EXTRA_OPEN_CONFIGURATION_DIALOG_ON_START)

        /**
         * In-App Theme
         */

        val inAppThemeState = NonAppliedStateFlow(
            viewModelScope,
            { getByOrdinal<Theme>(enumOrdinals.inAppTheme) },
            {
                enumOrdinals.inAppTheme = it.ordinal
                appliedInAppTheme.value = it
            }
        )

        var appliedInAppTheme = MutableStateFlow<Theme>(getByOrdinal(enumOrdinals.inAppTheme))

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

            if (widgetProperties.getValue(WifiProperty.SSID.name) && !context.locationServicesEnabled)
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

        val showWidgetConfigurationDialog = MutableStateFlow(false)

        val propertyInfoDialogIndex: MutableStateFlow<Int?> = MutableStateFlow(null)

        val widgetPropertyStateMap = NonAppliedSnapshotStateMap(
            { widgetProperties },
            { widgetProperties.putAll(it) }
        )

        val widgetThemeState = NonAppliedStateFlow(
            viewModelScope,
            { getByOrdinal<Theme>(enumOrdinals.widgetTheme) },
            { enumOrdinals.widgetTheme = it.ordinal }
        )

        val showCustomThemeSection = widgetThemeState.transform {
            emit(it == Theme.Custom)
        }

        val customWidgetColorsState = NonAppliedSnapshotStateMap(
            { customWidgetColors },
            { customWidgetColors.putAll(it) }
        )

        val customizationDialogSection = MutableStateFlow<CustomizableWidgetSection?>(null)

        fun onDismissCustomizationDialog() {
            customizationDialogSection.reset()
        }

        val widgetOpacityState = NonAppliedStateFlow(
            viewModelScope,
            { floatPreferences.opacity },
            { floatPreferences.opacity = it }
        )

        val widgetRefreshingParametersState = NonAppliedSnapshotStateMap(
            { widgetRefreshingParameters },
            {
                widgetRefreshingParameters.putAll(it)
                widgetRefreshingParametersChanged.value = true
            }
        )

        val widgetRefreshingParametersChanged = MutableStateFlow(false)

        val widgetConfigurationStates = CoherentNonAppliedStates(
            widgetPropertyStateMap,
            widgetThemeState,
            widgetOpacityState,
            widgetRefreshingParametersState,
            customWidgetColorsState,
            coroutineScope = viewModelScope
        )

        fun onDismissWidgetConfigurationDialog() {
            widgetConfigurationStates.reset()
            showWidgetConfigurationDialog.value = false
        }

        /**
         * @return Boolean indicating whether change has been confirmed
         */
        fun confirmAndSyncPropertyChange(
            property: WifiProperty,
            value: Boolean,
            onChangeRejected: () -> Unit
        ) {
            when (value || widgetPropertyStateMap.values.count { true } != 1) {
                true -> widgetPropertyStateMap[property.name] = value
                false -> onChangeRejected()
            }
        }

        /**
         * lap := Location Access Permission
         */

        var lapDialogAnswered: Boolean by globalFlags::locationPermissionDialogAnswered

        val lapDialogTrigger: MutableStateFlow<LocationAccessPermissionDialogTrigger?> =
            MutableStateFlow(null)

        /**
         * BackPress
         */

        var exitOnBackPress: Boolean = false
            private set

        fun onFirstBackPress(context: Context) {
            exitOnBackPress = true
            context.showToast("Tap again to exit")
            viewModelScope.launchDelayed(2500L) {
                exitOnBackPress = false
            }
        }
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
        LocationAccessPermissionHandler(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener(
            SwipeUpAnimation {
                viewModel.onSplashScreenAnimationFinished()
            }
        )

        super.onCreate(savedInstanceState)

        viewModel.lifecycleObservers + listOf(
            lapRequestLauncher,
            WifiWidgetOptionsChangedReceiver()
        )
            .forEach(lifecycle::addObserver)

        lifecycleScope.launch {
            with(viewModel.widgetRefreshingParametersChanged) {
                collect {
                    if (it) {
                        WidgetDataRefreshWorker.Administrator.getInstance(applicationContext)
                            .applyChangedParameters()
                    }
                    value = false
                }
            }
        }

        setContent {
            WifiWidgetTheme(
                darkTheme = when (viewModel.appliedInAppTheme.collectAsState().value) {
                    Theme.Light -> false
                    Theme.Dark -> true
                    Theme.DeviceDefault -> isSystemInDarkTheme()
                    else -> throw Error()
                }
            ) {
                HomeScreen {
                    finishAffinity()
                }
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