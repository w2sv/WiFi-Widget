package com.w2sv.wifiwidget.ui.viewmodels

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.androidutils.services.isLocationEnabled
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateFlow
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import com.w2sv.common.di.PackageName
import com.w2sv.common.utils.trigger
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.widget.WidgetDataRefreshWorker
import com.w2sv.widget.WidgetProvider
import com.w2sv.widget.utils.attemptWifiWidgetPin
import com.w2sv.widget.utils.getWifiWidgetIds
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.SharedSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.SnackbarAction
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.hasBackgroundLocationAccess
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.UnconfirmedWidgetConfiguration
import com.w2sv.wifiwidget.ui.utils.fromPersistedFlowMapWithSynchronousInitialAsMutableStateMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@HiltViewModel
class WidgetViewModel @Inject constructor(
    private val repository: WidgetRepository,
    private val widgetDataRefreshWorkerManager: WidgetDataRefreshWorker.Manager,
    private val appWidgetManager: AppWidgetManager,
    @PackageName private val packageName: String,
    private val resources: Resources,
    private val sharedSnackbarVisuals: SharedSnackbarVisuals,
    optionsChanged: WidgetProvider.OptionsChanged,
    @ApplicationContext context: Context
) :
    ViewModel() {

    init {
        viewModelScope.collectFromFlow(optionsChanged.widgetId) {
            if (widgetIds.add(it)) {
                i { "Pinned new widget w ID=$it" }
                onNewWidgetPinned(context)
            }
        }
    }

    fun onStart() {
        refreshWidgetIds()
    }

    fun attemptWidgetPin() {
        if (!appWidgetManager.attemptWifiWidgetPin(packageName)) {
            viewModelScope.launch {
                sharedSnackbarVisuals.emit(
                    AppSnackbarVisuals(
                        msg = resources.getString(com.w2sv.common.R.string.widget_pinning_not_supported_by_your_device_launcher),
                        kind = SnackbarKind.Error
                    )
                )
            }
        }
    }

    private var widgetIds: MutableSet<Int> = getWidgetIds()

    private fun refreshWidgetIds() {
        widgetIds = getWidgetIds()
    }

    private fun getWidgetIds(): MutableSet<Int> =
        appWidgetManager.getWifiWidgetIds(packageName).toMutableSet()

    val launchBackgroundLocationAccessPermissionRequest get() = _launchBackgroundLocationAccessPermissionRequest.asSharedFlow()
    private val _launchBackgroundLocationAccessPermissionRequest = MutableSharedFlow<Unit>()

    private fun onNewWidgetPinned(context: Context) {
        viewModelScope.launch {
            if (WidgetWifiProperty.NonIP.LocationAccessRequiring.entries
                    .any {
                        configuration.wifiProperties.persistedStateFlowMap.getValue(it).value
                    }
            ) {
                when {
                    !context.isLocationEnabled -> sharedSnackbarVisuals.emit(
                        AppSnackbarVisuals(
                            msg = context.getString(R.string.on_pin_widget_wo_gps_enabled),
                            kind = SnackbarKind.Error,
                        ),
                    )

                    !hasBackgroundLocationAccess(context) ->
                        sharedSnackbarVisuals.emit(
                            AppSnackbarVisuals(
                                msg = context.getString(R.string.on_pin_widget_wo_background_location_access_permission),
                                kind = SnackbarKind.Error,
                                action = SnackbarAction(
                                    label = resources.getString(R.string.grant),
                                    callback = {
                                        viewModelScope.launch {
                                            _launchBackgroundLocationAccessPermissionRequest.trigger()
                                        }
                                    }
                                )
                            ),
                        )
                }
            }
            sharedSnackbarVisuals.emit(
                AppSnackbarVisuals(
                    msg = context.getString(R.string.pinned_widget),
                    kind = SnackbarKind.Success,
                ),
            )
        }
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
            persistedFlowMap = repository.getIPSubPropertyEnablementMap(),
            scope = viewModelScope,
            syncState = { repository.saveIPSubPropertyEnablementMap(it) },
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
                widgetDataRefreshWorkerManager.applyChangedParameters()
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
        sharedSnackbarVisuals = sharedSnackbarVisuals,
        onStateSynced = {
            WidgetProvider.triggerDataRefresh(context)
            sharedSnackbarVisuals.emit(
                AppSnackbarVisuals(
                    msg = context.getString(R.string.updated_widget_configuration),
                    kind = SnackbarKind.Success,
                ),
            )
        },
    )
}
