package com.w2sv.wifiwidget.ui.viewmodels

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.compose.material3.SnackbarVisuals
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateFlow
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import com.w2sv.common.constants.Extra
import com.w2sv.common.di.PackageName
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.widget.WidgetDataRefreshWorker
import com.w2sv.widget.WidgetProvider
import com.w2sv.widget.utils.attemptWifiWidgetPin
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.di.SnackbarVisualsFlow
import com.w2sv.wifiwidget.di.WidgetPinSuccessFlow
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.UnconfirmedWidgetConfiguration
import com.w2sv.wifiwidget.ui.utils.fromStateFlowMap
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetViewModel @Inject constructor(
    private val repository: WidgetRepository,
    private val widgetDataRefreshWorkerManager: WidgetDataRefreshWorker.Manager,
    private val appWidgetManager: AppWidgetManager,
    @PackageName private val packageName: String,
    @SnackbarVisualsFlow private val sharedSnackbarVisuals: MutableSharedFlow<(Context) -> SnackbarVisuals>,
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle,
    @WidgetPinSuccessFlow widgetPinSuccessFlow: MutableSharedFlow<Unit>
) :
    ViewModel() {

    // =========
    // Pinning
    // =========

    val widgetPinSuccessFlow = widgetPinSuccessFlow.asSharedFlow()

    fun attemptWidgetPin(context: Context) {
        appWidgetManager.attemptWifiWidgetPin(
            packageName = packageName,
            successCallback = PendingIntent.getBroadcast(
                context,
                WidgetPinSuccessBroadcastReceiver.REQUEST_CODE,
                Intent(context, WidgetPinSuccessBroadcastReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE
            ),
            onFailure = {
                viewModelScope.launch {
                    sharedSnackbarVisuals.emit {
                        AppSnackbarVisuals(
                            msg = it.getString(R.string.widget_pinning_not_supported_by_your_device_launcher),
                            kind = SnackbarKind.Error
                        )
                    }
                }
            }
        )
    }

    // =========
    // Configuration
    // =========

    val showConfigurationDialogInitially =
        savedStateHandle.get<Boolean>(Extra.SHOW_WIDGET_CONFIGURATION_DIALOG) == true

    val configuration = UnconfirmedWidgetConfiguration(
        coloring = UnconfirmedStateFlow(
            coroutineScope = viewModelScope,
            dataStoreStateFlow = repository.coloring
        ),
        presetColoringData = UnconfirmedStateFlow(
            scope = viewModelScope,
            appliedStateFlow = repository.presetColoringData,
            syncState = { repository.savePresetColoringData(it) }
        ),
        customColoringData = UnconfirmedStateFlow(
            scope = viewModelScope,
            appliedStateFlow = repository.customColoringData,
            syncState = { repository.saveCustomColoringData(it) }
        ),
        opacity = UnconfirmedStateFlow(
            coroutineScope = viewModelScope,
            dataStoreStateFlow = repository.opacity
        ),
        fontSize = UnconfirmedStateFlow(
            coroutineScope = viewModelScope,
            dataStoreStateFlow = repository.fontSize
        ),
        wifiProperties = UnconfirmedStateMap.fromStateFlowMap(
            stateFlowMap = repository.wifiPropertyEnablementMap,
            syncState = { repository.saveWifiPropertyEnablementMap(it) },
        ),
        ipSubProperties = UnconfirmedStateMap.fromStateFlowMap(
            stateFlowMap = repository.ipSubPropertyEnablementMap,
            syncState = { repository.saveIPSubPropertyEnablementMap(it) },
        ),
        bottomRowMap = UnconfirmedStateMap.fromStateFlowMap(
            stateFlowMap = repository.bottomRowElementEnablementMap,
            syncState = {
                repository.saveBottomRowElementEnablementMap(it)
            },
        ),
        refreshingParametersMap = UnconfirmedStateMap.fromStateFlowMap(
            stateFlowMap = repository.refreshingParametersEnablementMap,
            syncState = {
                repository.saveRefreshingParametersEnablementMap(it)
                widgetDataRefreshWorkerManager.enableWorkerIfRefreshingEnabled()
            },
        ),
        scope = viewModelScope,
        mutableSharedSnackbarVisuals = sharedSnackbarVisuals,
        onStateSynced = {
            WidgetProvider.triggerDataRefresh(context)
            sharedSnackbarVisuals.emit {
                AppSnackbarVisuals(
                    msg = it.getString(R.string.updated_widget_configuration),
                    kind = SnackbarKind.Success,
                )
            }
        },
    )
}

@AndroidEntryPoint
class WidgetPinSuccessBroadcastReceiver : BroadcastReceiver() {

    @Inject
    @WidgetPinSuccessFlow
    lateinit var widgetPinSuccessFlow: MutableSharedFlow<Unit>

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onReceive(p0: Context?, p1: Intent?) {
        scope.launch { widgetPinSuccessFlow.emit(Unit) }
    }

    companion object {
        const val REQUEST_CODE = 1447
    }
}
