package com.w2sv.wifiwidget.ui.viewmodels

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.eventhandling.BackPressHandler
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.androidutils.permissions.hasPermission
import com.w2sv.androidutils.services.isLocationEnabled
import com.w2sv.common.constants.Extra
import com.w2sv.data.model.WifiProperty
import com.w2sv.data.networking.WifiStatusMonitor
import com.w2sv.data.storage.PreferencesRepository
import com.w2sv.data.storage.WidgetRepository
import com.w2sv.widget.di.PackageName
import com.w2sv.widget.utils.getWifiWidgetIds
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.components.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionUIState
import com.w2sv.wifiwidget.ui.screens.home.components.wifi_status.WifiStatusUIState
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
    private val widgetRepository: WidgetRepository,
    private val appWidgetManager: AppWidgetManager,
    @PackageName private val packageName: String,
    @ApplicationContext context: Context,
    wifiPropertyValueGetterResourcesProvider: WifiProperty.ValueGetterResources.Provider,
    wifiStatusMonitor: WifiStatusMonitor,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    fun onSplashScreenAnimationFinished() {
        if (savedStateHandle.contains(Extra.OPEN_WIDGET_CONFIGURATION_DIALOG)) {
            showWidgetConfigurationDialog.value = true
        }
    }

    val showWidgetConfigurationDialog = MutableStateFlow(false)

    val snackbarHostState = SnackbarHostState()

    fun onWidgetConfigurationChanged(context: Context) {
        viewModelScope.launch {
            snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                AppSnackbarVisuals(
                    message = context.getString(R.string.updated_widget_configuration),
                    kind = SnackbarKind.Success
                )
            )
        }
    }

    fun onStart(context: Context) {
        wifiStatusUIState.triggerPropertiesViewDataRefresh()
        refreshWidgetIds()
        lapUIState.updateBackgroundAccessGranted(context = context)
    }

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
                        AppSnackbarVisuals(
                            context.getString(R.string.on_pin_widget_wo_gps_enabled),
                            kind = SnackbarKind.Error
                        )
                    )

                    !context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) -> snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                        AppSnackbarVisuals(
                            context.getString(R.string.on_pin_widget_wo_location_access_permission),
                            kind = SnackbarKind.Error
                        )
                    )

                    !lapUIState.backgroundLocationAccessGranted ->
                        snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                            AppSnackbarVisuals(
                                context.getString(R.string.on_pin_widget_wo_background_location_access_permission),
                                kind = SnackbarKind.Error
                            )
                        )
                }

            snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                AppSnackbarVisuals(
                    message = context.getString(R.string.pinned_widget),
                    kind = SnackbarKind.Success
                )
            )
        }
    }

    private var widgetIds: MutableSet<Int> = getWidgetIds()

    private fun refreshWidgetIds() {
        widgetIds = getWidgetIds()
    }

    private fun getWidgetIds(): MutableSet<Int> =
        appWidgetManager.getWifiWidgetIds(packageName).toMutableSet()

    val lapUIState = LocationAccessPermissionUIState(
        preferencesRepository = preferencesRepository,
        snackbarHostState = snackbarHostState,
        scope = viewModelScope,
        context = context
    )
        .apply {
            viewModelScope.launch {
                newlyGranted.collect {
                    if (it) {
                        wifiStatusUIState.triggerPropertiesViewDataRefresh()
                    }
                }
            }
        }

    val wifiStatusUIState = WifiStatusUIState(
        wifiPropertyValueGetterResourcesProvider,
        wifiStatusMonitor,
        viewModelScope
    )

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