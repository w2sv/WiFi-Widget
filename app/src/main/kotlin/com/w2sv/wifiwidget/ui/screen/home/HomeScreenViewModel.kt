package com.w2sv.wifiwidget.ui.screen.home

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.location.isLocationEnabledCompat
import com.w2sv.androidutils.service.systemService
import com.w2sv.common.utils.log
import com.w2sv.core.common.R
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.domain.repository.RemoteNetworkInfoRepository
import com.w2sv.domain.repository.WidgetConfigDataSource
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.networking.wifistatus.WifiStatusMonitor
import com.w2sv.widget.di.WidgetPinSuccessFlow
import com.w2sv.widget.utils.attemptWifiWidgetPin
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.screen.home.wifistatus.model.WifiState
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    wifiStatusMonitor: WifiStatusMonitor,
    wifiPropertyViewDataProvider: WifiPropertyViewDataProvider,
    remoteNetworkInfoRepository: RemoteNetworkInfoRepository,
    private val widgetConfigDataSource: WidgetConfigDataSource,
    private val appWidgetManager: AppWidgetManager,
    @WidgetPinSuccessFlow val widgetPinSuccessFlow: SharedFlow<Unit>,
    @ApplicationContext val context: Context
) : ViewModel() {

    private val _snackbarBuilder = MutableSharedFlow<SnackbarBuilder>()
    val snackbarBuilder = _snackbarBuilder.asSharedFlow()

    private val widgetConfigFlow = widgetConfigDataSource.config

    /**
     * For computation of a new wifiState upon location access change.
     */
    private val locationAccessChanged = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val isAnyLocationAccessRequiringPropertyEnabled: Boolean get() = _isAnyLocationAccessRequiringPropertyEnabled.value
    private val _isAnyLocationAccessRequiringPropertyEnabled = widgetConfigFlow
        .map { it.isAnyLocationAccessRequiringPropertyEnabled }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false
        )

    val wifiState: StateFlow<WifiState> = wifiStatusMonitor.wifiStatus
        .flatMapLatest { wifiStatus ->
            when (wifiStatus) {
                WifiStatus.Disabled -> flowOf(WifiState.Disabled)
                WifiStatus.Disconnected -> flowOf(WifiState.Disconnected)
                WifiStatus.Connected, WifiStatus.ConnectedInactive -> connectedWifiState
            }
                .log { "Set wifiState=$it" }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), WifiState.Disconnected)

    private val connectedWifiState = merge(
        widgetConfigFlow,
        combine(locationAccessChanged, widgetConfigFlow) { _, config -> config }
            .filter { it.isAnyLocationAccessRequiringPropertyEnabled }
    )
        .combine(remoteNetworkInfoRepository.data) { a, b -> a to b }
        .map { (config, remoteNetworkInfo) ->
            WifiState.Connected(
                propertyViewData = wifiPropertyViewDataProvider(
                    enabledProperties = config.orderedEnabledProperties(),
                    enabledIpSettings = config::enabledIpSettings,
                    remoteNetworkInfo = remoteNetworkInfo
                )
            )
        }

    init {
        widgetConfigFlow.collectOn(viewModelScope) {
            remoteNetworkInfoRepository.refresh()
        }

        context
            .isGpsEnabledFlow()
            .distinctUntilChanged()
            .collectOn(viewModelScope) {
                locationAccessChanged.emit(Unit)
            }
    }

    fun pinWidget() {
        appWidgetManager.attemptWifiWidgetPin(
            context = context,
            onFailure = {
                viewModelScope.launch {
                    _snackbarBuilder.emit {
                        AppSnackbarVisuals(
                            msg = getString(R.string.widget_pinning_failed),
                            kind = SnackbarKind.Warning
                        )
                    }
                }
            }
        )
    }

    fun onLocationAccessChanged() {
        viewModelScope.launch { locationAccessChanged.emit(Unit) }
    }

    fun enableLocationAccessRequiringProperties() {
        viewModelScope.launch { widgetConfigDataSource.update { it.withEnabledLocationAccessRequiringProperties() } }
    }
}

private fun Context.isGpsEnabledFlow(): Flow<Boolean> = callbackFlow {
    val locationManager = systemService<LocationManager>().also { trySend(it.isLocationEnabledCompat()) }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            trySend(locationManager.isLocationEnabledCompat())
        }
    }

    registerReceiver(receiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))
    awaitClose { unregisterReceiver(receiver) }
}
