package com.w2sv.wifiwidget.ui.viewmodels

import android.Manifest
import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.androidutils.eventhandling.BackPressHandler
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.androidutils.permissions.hasPermission
import com.w2sv.androidutils.services.isLocationEnabled
import com.w2sv.common.constants.Extra
import com.w2sv.data.model.WifiProperty
import com.w2sv.data.model.WifiStatus
import com.w2sv.data.networking.WifiStatusMonitor
import com.w2sv.data.storage.PreferencesRepository
import com.w2sv.data.storage.WidgetRepository
import com.w2sv.widget.utils.getWifiWidgetIds
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.ExtendedSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.components.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LAPRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.backgroundLocationAccessGrantRequired
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val widgetRepository: WidgetRepository,
    private val wifiStatusMonitor: WifiStatusMonitor,
    @ApplicationContext context: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    fun onSplashScreenAnimationFinished() {
        if (savedStateHandle.contains(Extra.OPEN_WIDGET_CONFIGURATION_DIALOG)) {
            showWidgetConfigurationDialog.value = true
        }
    }

    val showWidgetConfigurationDialog = MutableStateFlow(false)

    val snackbarHostState = SnackbarHostState()

    fun onWidgetConfigurationChanged() {
        viewModelScope.launch {
            snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                ExtendedSnackbarVisuals(
                    message = buildString {
                        append("Updated configuration")
                        if (widgetIds.isNotEmpty()) {
                            append(" of ${widgetIds.size} widget(s)")
                        }
                    },
                    kind = SnackbarKind.Success
                )
            )
        }
    }
    val wifiStatus = wifiStatusMonitor.wifiStatus.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        WifiStatus.Disabled
    )

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

        viewModelScope.launch {
            if (widgetRepository.wifiProperties.getValue(WifiProperty.SSID)
                    .first() || widgetRepository.wifiProperties.getValue(WifiProperty.BSSID).first()
            )
                when {
                    !context.isLocationEnabled -> snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                        ExtendedSnackbarVisuals(
                            context.getString(R.string.on_pin_widget_wo_gps_enabled),
                            SnackbarKind.Error
                        )
                    )

                    !context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) -> snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                        ExtendedSnackbarVisuals(
                            context.getString(R.string.on_pin_widget_wo_location_access_permission),
                            SnackbarKind.Error
                        )
                    )

                    (backgroundLocationAccessGrantRequired) && !context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) && !showBackgroundLocationAccessRational.value ->
                        snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                            ExtendedSnackbarVisuals(
                                context.getString(R.string.on_pin_widget_wo_background_location_access_permission),
                                SnackbarKind.Error
                            )
                        )
                }

            snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                ExtendedSnackbarVisuals(
                    message = context.getString(R.string.pinned_widget),
                    kind = SnackbarKind.Success
                )
            )
        }
    }

    private val widgetIds: MutableSet<Int> =
        getWifiWidgetIds(context).toMutableSet()

    // =============
    // LAP := Location Access Permission
    // =============

    val lapRationalTrigger: MutableStateFlow<LAPRequestTrigger?> =
        MutableStateFlow(null)

    val lapRationalShown: Boolean
        get() = preferencesRepository.locationAccessPermissionRationalShown.getValueSynchronously()

    fun onLocationAccessPermissionRationalShown(trigger: LAPRequestTrigger) {
        viewModelScope.launch {
            preferencesRepository.saveLocationAccessPermissionRationalShown(true)
        }
        lapRationalTrigger.value = null
        lapRequestTrigger.value = trigger
    }

    val lapRequestTrigger: MutableStateFlow<LAPRequestTrigger?> =
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