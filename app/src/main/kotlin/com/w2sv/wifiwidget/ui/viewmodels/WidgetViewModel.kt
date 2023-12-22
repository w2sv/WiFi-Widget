package com.w2sv.wifiwidget.ui.viewmodels

import android.appwidget.AppWidgetManager
import android.content.Context
import android.location.LocationManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateFlow
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import com.w2sv.common.constants.Extra
import com.w2sv.common.di.PackageName
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.widget.WidgetDataRefreshWorker
import com.w2sv.widget.WidgetProvider
import com.w2sv.widget.utils.attemptWifiWidgetPin
import com.w2sv.widget.utils.getWifiWidgetIds
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.di.MutableSharedSnackbarVisualsFlow
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.UnconfirmedWidgetConfiguration
import com.w2sv.wifiwidget.ui.utils.fromPersistedFlowMapWithSynchronousInitialAsMutableStateMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@HiltViewModel
class WidgetViewModel @Inject constructor(
    val locationManager: LocationManager,
    private val repository: WidgetRepository,
    private val widgetDataRefreshWorkerManager: WidgetDataRefreshWorker.Manager,
    private val appWidgetManager: AppWidgetManager,
    @PackageName private val packageName: String,
    private val mutableSharedSnackbarVisuals: MutableSharedSnackbarVisualsFlow,
    optionsChanged: WidgetProvider.OptionsChanged,
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    // =========
    // IDs
    // =========

    private var widgetIds: MutableSet<Int> = getWidgetIds()

    fun refreshWidgetIds() {
        widgetIds = getWidgetIds()
    }

    private fun getWidgetIds(): MutableSet<Int> =
        appWidgetManager.getWifiWidgetIds(packageName).toMutableSet()

    // =========
    // Pinning
    // =========

    fun attemptWidgetPin() {
        if (!appWidgetManager.attemptWifiWidgetPin(packageName)) {
            viewModelScope.launch {
                mutableSharedSnackbarVisuals.emit {
                    AppSnackbarVisuals(
                        msg = it.getString(com.w2sv.common.R.string.widget_pinning_not_supported_by_your_device_launcher),
                        kind = SnackbarKind.Error
                    )
                }
            }
        }
    }

    val newWidgetPinned get() = _newWidgetPinned.asSharedFlow()
    private val _newWidgetPinned = MutableSharedFlow<Unit>()

    init {
        viewModelScope.collectFromFlow(optionsChanged.widgetId) {
            if (widgetIds.add(it)) {
                _newWidgetPinned.emit(Unit)
                i { "Pinned new widget w ID=$it" }
            }
        }
    }

    // =========
    // Configuration
    // =========

    val showConfigurationDialog get() = _showConfigurationDialog.asStateFlow()
    private val _showConfigurationDialog =
        MutableStateFlow(savedStateHandle.get<Boolean>(Extra.OPEN_WIDGET_CONFIGURATION_DIALOG) == true)

    fun setShowConfigurationDialog(value: Boolean) {
        _showConfigurationDialog.value = value
    }

    val configuration = UnconfirmedWidgetConfiguration(
        wifiProperties = UnconfirmedStateMap.fromPersistedFlowMapWithSynchronousInitialAsMutableStateMap(
            persistedFlowMap = repository.getWifiPropertyEnablementMap(),
            scope = viewModelScope,
            syncState = { repository.saveWifiPropertyEnablementMap(it) },
        ),
        ipSubProperties = UnconfirmedStateMap.fromPersistedFlowMapWithSynchronousInitialAsMutableStateMap(
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
        mutableSharedSnackbarVisuals = mutableSharedSnackbarVisuals,
        onStateSynced = {
            WidgetProvider.triggerDataRefresh(context)
            mutableSharedSnackbarVisuals.emit {
                AppSnackbarVisuals(
                    msg = it.getString(R.string.updated_widget_configuration),
                    kind = SnackbarKind.Success,
                )
            }
        },
    )
}
