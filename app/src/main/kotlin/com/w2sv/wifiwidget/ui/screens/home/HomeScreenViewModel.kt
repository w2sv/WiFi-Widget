package com.w2sv.wifiwidget.ui.screens.home

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.androidutils.eventhandling.BackPressHandler
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.androidutils.permissions.hasPermission
import com.w2sv.androidutils.services.isLocationEnabled
import com.w2sv.androidutils.ui.resources.getLong
import com.w2sv.common.data.repositories.PreferencesRepository
import com.w2sv.common.data.repositories.WidgetConfigurationRepository
import com.w2sv.common.enums.WifiProperty
import com.w2sv.widget.WidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.backgroundLocationAccessGrantRequired
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequestTrigger
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
    preferencesRepository: PreferencesRepository,
    private val widgetConfigurationRepository: WidgetConfigurationRepository,
    @ApplicationContext context: Context,
    private val savedStateHandle: SavedStateHandle
) : PreferencesDataStoreRepository.ViewModel<PreferencesRepository>(preferencesRepository) {

    fun onSplashScreenAnimationFinished() {
        if (savedStateHandle.contains(WidgetProvider.EXTRA_OPEN_CONFIGURATION_DIALOG_ON_START)) {
            showWidgetConfigurationDialog.value = true
        }
    }

    val showWidgetConfigurationDialog = MutableStateFlow(false)

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
            if (widgetConfigurationRepository.wifiProperties.getValue(WifiProperty.SSID).first())
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
        WidgetProvider.getWidgetIds(context).toMutableSet()

    // =============
    // LAP := Location Access Permission
    // =============

    val lapRationalTrigger: MutableStateFlow<LocationAccessPermissionRequestTrigger?> =
        MutableStateFlow(null)

    val lapRationalShown: Boolean
        get() = repository.locationAccessPermissionRationalShown.getValueSynchronously()

    val lapRequestTrigger: MutableStateFlow<LocationAccessPermissionRequestTrigger?> =
        MutableStateFlow(null)

    val lapRequestLaunchedAtLeastOnce: Boolean
        get() = repository.locationAccessPermissionRequestedAtLeastOnce.getValueSynchronously()

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
        context.resources.getLong(R.integer.backpress_confirmation_window)
    )
}