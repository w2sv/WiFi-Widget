package com.w2sv.wifiwidget.ui.viewmodels

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.material3.SnackbarVisuals
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.permissions.hasPermission
import com.w2sv.androidutils.services.isLocationEnabled
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateFlow
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import com.w2sv.common.di.PackageName
import com.w2sv.data.repositories.WidgetRepository
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.widget.WidgetDataRefreshWorker
import com.w2sv.widget.WidgetProvider
import com.w2sv.widget.utils.getWifiWidgetIds
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.hasBackgroundLocationAccess
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyInfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.UnconfirmedWidgetConfiguration
import com.w2sv.wifiwidget.ui.utils.fromPersistedFlowMapWithSynchronousInitialAsMutableStateMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    @ApplicationContext context: Context,
) :
    ViewModel() {

    init {
        viewModelScope.launch {
            repository.optionsChangedWidgetId.collect {
                if (widgetIds.add(it)) {
                    onNewWidgetPinned(it, context)
                }
            }
        }
    }

    val snackbarVisuals get() = _snackbarVisuals.asSharedFlow()
    private val _snackbarVisuals = MutableSharedFlow<SnackbarVisuals>()

    private var widgetIds: MutableSet<Int> = getWidgetIds()

    fun refreshWidgetIds() {
        widgetIds = getWidgetIds()
    }

    private fun getWidgetIds(): MutableSet<Int> =
        appWidgetManager.getWifiWidgetIds(packageName).toMutableSet()

    private fun onNewWidgetPinned(widgetId: Int, context: Context) {
        i { "Pinned new widget w ID=$widgetId" }

        viewModelScope.launch {
            if (configuration.wifiProperties.getValue(WidgetWifiProperty.SSID) || configuration.wifiProperties.getValue(
                    WidgetWifiProperty.BSSID,
                )
            ) {
                when {
                    !context.isLocationEnabled -> _snackbarVisuals.emit(
                        AppSnackbarVisuals(
                            context.getString(R.string.on_pin_widget_wo_gps_enabled),
                            kind = SnackbarKind.Error,
                        ),
                    )

                    !context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) -> _snackbarVisuals.emit(
                        AppSnackbarVisuals(
                            context.getString(R.string.on_pin_widget_wo_location_access_permission),
                            kind = SnackbarKind.Error,
                        ),
                    )

                    !hasBackgroundLocationAccess(context) ->
                        _snackbarVisuals.emit(
                            AppSnackbarVisuals(
                                context.getString(R.string.on_pin_widget_wo_background_location_access_permission),
                                kind = SnackbarKind.Error,
                            ),
                        )
                }
            }

            _snackbarVisuals.emit(
                AppSnackbarVisuals(
                    message = context.getString(R.string.pinned_widget),
                    kind = SnackbarKind.Success,
                ),
            )
        }
    }

    // ========================
    // Overlay dialogs
    // ========================

    val propertyInfoDialogData get() = _propertyInfoDialogData.asStateFlow()
    private val _propertyInfoDialogData = MutableStateFlow<PropertyInfoDialogData?>(null)

    fun setPropertyInfoDialogData(propertyInfoDialogData: PropertyInfoDialogData?) {
        _propertyInfoDialogData.value = propertyInfoDialogData
    }

    // =========
    // Configuration
    // =========

    val configuration = UnconfirmedWidgetConfiguration(
        wifiProperties = UnconfirmedStateMap.fromPersistedFlowMapWithSynchronousInitialAsMutableStateMap(
            persistedFlowMap = repository.getWifiPropertyEnablementMap(),
            scope = viewModelScope,
            syncState = { repository.saveWifiPropertyEnablementMap(it) },
        ),
        subWifiProperties = UnconfirmedStateMap.fromPersistedFlowMapWithSynchronousInitialAsMutableStateMap(
            persistedFlowMap = repository.getEnabledAddressTypesMap(),
            scope = viewModelScope,
            syncState = { repository.saveEnabledAddressTypesMap(it) },
        ),
        buttonMap = UnconfirmedStateMap.fromPersistedFlowMapWithSynchronousInitialAsMutableStateMap(
            persistedFlowMap = repository.getButtonEnablementMap(),
            scope = viewModelScope,
            syncState = {
                repository.saveButtonEnablementMap(it)
            },
        ),
        refreshingParametersMap = UnconfirmedStateMap.fromPersistedFlowMapWithSynchronousInitialAsMutableStateMap(
            persistedFlowMap = repository.getRefreshingParametersEnablementMap(),
            scope = viewModelScope,
            syncState = {
                repository.saveRefreshingParametersEnablementMap(it)
                withContext(Dispatchers.IO) {
                    widgetDataRefreshWorkerManager.applyChangedParameters()
                }
            },
        ),
        useDynamicColors = UnconfirmedStateFlow(
            coroutineScope = viewModelScope,
            persistedValue = repository.useDynamicColors
        ),
        theme = UnconfirmedStateFlow(
            coroutineScope = viewModelScope,
            persistedValue = repository.theme
        ),
        customColorsMap = UnconfirmedStateMap.fromPersistedFlowMapWithSynchronousInitialAsMutableStateMap(
            persistedFlowMap = repository.getCustomColorsMap(),
            scope = viewModelScope,
            syncState = { repository.saveCustomColorsMap(it) },
        ),
        opacity = UnconfirmedStateFlow(
            coroutineScope = viewModelScope,
            persistedValue = repository.opacity
        ),
        scope = viewModelScope,
    )

    fun syncConfiguration(context: Context) {
        viewModelScope.launch {
            configuration.sync()
            WidgetProvider.triggerDataRefresh(context)
            _snackbarVisuals.emit(
                AppSnackbarVisuals(
                    message = context.getString(R.string.updated_widget_configuration),
                    kind = SnackbarKind.Success,
                ),
            )
        }
    }
}
