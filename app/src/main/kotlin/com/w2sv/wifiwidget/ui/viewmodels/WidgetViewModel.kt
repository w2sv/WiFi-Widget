package com.w2sv.wifiwidget.ui.viewmodels

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.material3.SnackbarVisuals
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.permissions.hasPermission
import com.w2sv.androidutils.services.isLocationEnabled
import com.w2sv.androidutils.ui.unconfirmed_state.getUnconfirmedStateFlow
import com.w2sv.data.model.WifiProperty
import com.w2sv.data.storage.WidgetRepository
import com.w2sv.widget.WidgetDataRefreshWorker
import com.w2sv.widget.WidgetProvider
import com.w2sv.widget.di.PackageName
import com.w2sv.widget.utils.getWifiWidgetIds
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.screens.home.components.location_access_permission.hasBackgroundLocationAccess
import com.w2sv.wifiwidget.ui.screens.home.components.widget_configuration_dialog.model.PropertyInfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widget_configuration_dialog.model.UnconfirmedWidgetConfiguration
import com.w2sv.wifiwidget.ui.utils.getUnconfirmedStateMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import slimber.log.i
import javax.inject.Inject

@HiltViewModel
class WidgetViewModel @Inject constructor(
    private val repository: WidgetRepository,
    private val widgetDataRefreshWorkerManager: WidgetDataRefreshWorker.Manager,
    private val appWidgetManager: AppWidgetManager,
    @PackageName private val packageName: String,
) :
    ViewModel() {

    val snackbarVisuals: Flow<SnackbarVisuals> get() = _snackbarVisuals.asSharedFlow()
    private val _snackbarVisuals = MutableSharedFlow<SnackbarVisuals>()

    private var widgetIds: MutableSet<Int> = getWidgetIds()

    fun refreshWidgetIds() {
        widgetIds = getWidgetIds()
    }

    private fun getWidgetIds(): MutableSet<Int> =
        appWidgetManager.getWifiWidgetIds(packageName).toMutableSet()

    fun onWidgetOptionsUpdated(widgetId: Int, context: Context) {
        if (widgetIds.add(widgetId)) {
            onNewWidgetPinned(widgetId, context)
        }
    }

    private fun onNewWidgetPinned(widgetId: Int, context: Context) {
        i { "Pinned new widget w ID=$widgetId" }

        viewModelScope.launch {
            if (configuration.wifiProperties.getValue(WifiProperty.SSID) || configuration.wifiProperties.getValue(
                    WifiProperty.BSSID
                )
            )
                when {
                    !context.isLocationEnabled -> _snackbarVisuals.emit(
                        AppSnackbarVisuals(
                            context.getString(R.string.on_pin_widget_wo_gps_enabled),
                            kind = SnackbarKind.Error
                        )
                    )

                    !context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) -> _snackbarVisuals.emit(
                        AppSnackbarVisuals(
                            context.getString(R.string.on_pin_widget_wo_location_access_permission),
                            kind = SnackbarKind.Error
                        )
                    )

                    !hasBackgroundLocationAccess(context) ->
                        _snackbarVisuals.emit(
                            AppSnackbarVisuals(
                                context.getString(R.string.on_pin_widget_wo_background_location_access_permission),
                                kind = SnackbarKind.Error
                            )
                        )
                }

            _snackbarVisuals.emit(
                AppSnackbarVisuals(
                    message = context.getString(R.string.pinned_widget),
                    kind = SnackbarKind.Success
                )
            )
        }
    }

    // ========================
    // Overlay dialogs
    // ========================

    val propertyInfoDialogData: MutableStateFlow<PropertyInfoDialogData?> = MutableStateFlow(null)

    // =========
    // Configuration
    // =========

    val configuration by lazy {
        UnconfirmedWidgetConfiguration(
            getUnconfirmedStateMap(
                appliedFlowMap = repository.wifiProperties,
                syncState = { repository.saveMap(it) }
            ),
            getUnconfirmedStateMap(
                repository.subWifiProperties,
                syncState = { repository.saveMap(it) }
            ),
            getUnconfirmedStateMap(
                appliedFlowMap = repository.buttonMap,
                syncState = {
                    repository.saveMap(it)
                }
            ),
            getUnconfirmedStateMap(
                appliedFlowMap = repository.refreshingParametersMap,
                syncState = {
                    repository.saveMap(it)
                    withContext(Dispatchers.IO) {
                        widgetDataRefreshWorkerManager.applyChangedParameters()
                    }
                }
            ),
            getUnconfirmedStateFlow(
                appliedFlow = repository.useDynamicColors,
                syncState = {
                    repository.savUseDynamicColors(it)
                }
            ),
            getUnconfirmedStateFlow(
                appliedFlow = repository.theme,
                syncState = {
                    repository.saveTheme(it)
                }
            ),
            getUnconfirmedStateMap(
                appliedFlowMap = repository.customColorsMap,
                syncState = { repository.saveMap(it) }
            ),
            getUnconfirmedStateFlow(
                appliedFlow = repository.opacity,
                syncState = { repository.saveOpacity(it) }
            ),
            scope = viewModelScope
        )
    }

    fun syncConfiguration(context: Context) {
        viewModelScope.launch {
            configuration.sync()
            WidgetProvider.triggerDataRefresh(context)
            _snackbarVisuals.emit(
                AppSnackbarVisuals(
                    message = context.getString(R.string.updated_widget_configuration),
                    kind = SnackbarKind.Success
                )
            )
        }
    }
}