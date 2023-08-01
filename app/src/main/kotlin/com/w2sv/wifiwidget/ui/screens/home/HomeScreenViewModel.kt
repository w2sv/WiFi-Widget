package com.w2sv.wifiwidget.ui.screens.home

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.androidutils.coroutines.reset
import com.w2sv.androidutils.eventhandling.BackPressHandler
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.androidutils.permissions.hasPermission
import com.w2sv.androidutils.services.isLocationEnabled
import com.w2sv.common.constants.Extra
import com.w2sv.data.model.WifiProperty
import com.w2sv.data.storage.PreferencesRepository
import com.w2sv.data.storage.WidgetRepository
import com.w2sv.widget.utils.getWifiWidgetIds
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.backgroundLocationAccessGrantRequired
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val widgetRepository: WidgetRepository,
    @ApplicationContext context: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    fun onSplashScreenAnimationFinished() {
        if (savedStateHandle.contains(Extra.OPEN_WIDGET_CONFIGURATION_DIALOG)) {
            showWidgetConfigurationDialog.value = true
        }
    }

    val showWidgetConfigurationDialog = MutableStateFlow(false)

    val inAppTheme by preferencesRepository::inAppTheme

    // ========================
    // Widget Pin Listening
    // ========================

    fun onWidgetOptionsUpdated(widgetId: Int, context: Context) {
        if (widgetIds.add(widgetId)) {
            onNewWidgetPinned(widgetId, context)
        }
    }

    private fun onNewWidgetPinned(widgetId: Int, context: Context) {
        i { "Pinned new widget w ID=$widgetId" }
        context.showToast(R.string.pinned_widget)

        viewModelScope.launch {
            if (widgetRepository.wifiProperties.getValue(WifiProperty.SSID).first())
                when {
                    !context.isLocationEnabled -> context.showToast(
                        R.string.on_pin_widget_wo_gps_enabled,
                        Toast.LENGTH_LONG
                    )

                    !context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) -> context.showToast(
                        R.string.on_pin_widget_wo_location_access_permission,
                        Toast.LENGTH_LONG
                    )

                    (backgroundLocationAccessGrantRequired) && !context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) && !showBackgroundLocationAccessRational.value -> context.showToast(
                        R.string.on_pin_widget_wo_background_location_access_permission,
                        Toast.LENGTH_LONG
                    )
                }
        }
    }

    private val widgetIds: MutableSet<Int> =
        getWifiWidgetIds(context).toMutableSet()

    // =============
    // LAP := Location Access Permission
    // =============

    val lapRationalTrigger: MutableStateFlow<LocationAccessPermissionRequestTrigger?> =
        MutableStateFlow(null)

    val lapRationalShown: Boolean
        get() = preferencesRepository.locationAccessPermissionRationalShown.getValueSynchronously()

    fun onLocationAccessPermissionRationalShown(trigger: LocationAccessPermissionRequestTrigger) {
        viewModelScope.launch {
            preferencesRepository.saveLocationAccessPermissionRationalShown(true)
        }
        lapRationalTrigger.reset()
        lapRequestTrigger.value = trigger
    }

    val lapRequestTrigger: MutableStateFlow<LocationAccessPermissionRequestTrigger?> =
        MutableStateFlow(null)

    val lapRequestLaunchedAtLeastOnce: Boolean
        get() = preferencesRepository.locationAccessPermissionRequestedAtLeastOnce.getValueSynchronously()

    fun onLocationAccessPermissionRequested() {
        viewModelScope.launch {
            preferencesRepository.saveLocationAccessPermissionRequestedAtLeastOnce(true)
        }
    }

    val showBackgroundLocationAccessRational = MutableStateFlow(false)

    // ==============
    // BackPress Handling
    // ==============

    val exitApplication = MutableSharedFlow<Unit>()

    fun onBackPress(context: Context) {
        backPressHandler.invoke(
            onFirstPress = {
                context.showToast(context.getString(R.string.tap_again_to_exit))
            },
            onSecondPress = {
                viewModelScope.launch {
                    exitApplication.emit(Unit)
                }
            }
        )
    }

    private val backPressHandler = BackPressHandler(
        viewModelScope,
        2500L
    )
}