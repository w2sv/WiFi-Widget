package com.w2sv.wifiwidget.ui.screen.home.model.wifistate

import com.hoc081098.flowext.withLatestFrom
import com.w2sv.common.utils.logOnCancellation
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentationProvider
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.kotlinutils.makeIf
import com.w2sv.wifiwidget.ui.screen.home.model.gpsstatus.LocationEnabledProvider
import com.w2sv.wifiwidget.ui.screen.home.synchronization.RemoteDataSynchronizer
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import slimber.log.d

/**
 * Combines reconciled remote requirements with independently observed presentation configuration,
 * published remote data, and location access. A changed requirement holds the previous displayed
 * state until synchronization catches up; local presentation changes need no repository work.
 */
@ViewModelScoped
class WifiStateProviderImpl @Inject constructor(
    remoteDataSynchronizer: RemoteDataSynchronizer,
    widgetConfigSource: WidgetConfigSource,
    wifiPropertyPresentationProvider: WifiPropertyPresentationProvider,
    remoteNetworkDataRepository: RemoteNetworkDataRepository,
    locationEnabledProvider: LocationEnabledProvider,
    private val scope: CoroutineScope
) : WifiStateProvider {
    private val locationAccessChanged = MutableSharedFlow<Unit>().apply {
        locationEnabledProvider.isEnabled
            .distinctUntilChanged()
            .collectOn(scope) { emit(Unit) }
    }

    private val presentationConfig = widgetConfigSource.flow
        .distinctUntilChangedBy { it.propertyConfig to it.propertyOrder }

    private val locationAccessChangedWhileDependentPropertiesEnabled: Flow<Unit> = locationAccessChanged
        .withLatestFrom(presentationConfig) { _, config -> config }
        .filter { it.isAnyLocationAccessRequiringPropertyEnabled }
        .map { }
        .onStart { emit(Unit) }

    private val displayInputs = combine(
        remoteDataSynchronizer.inputs,
        presentationConfig,
        remoteNetworkDataRepository.data,
        locationAccessChangedWhileDependentPropertiesEnabled
    ) { reconciled, config, remoteData, _ ->
        makeIf(reconciled.remoteDataConfig == config.remoteDataConfig) {
            DisplayInputs(status = reconciled.status, config = config, remoteData = remoteData)
        }
    }.filterNotNull()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val wifiState: StateFlow<WifiState> = displayInputs
        .mapLatest { inputs ->
            when (inputs.status) {
                WifiStatus.Disabled -> WifiState.Disabled
                WifiStatus.NotConnected -> WifiState.Disconnected
                else -> {
                    d { "Computing connected Wi-Fi state for ${inputs.status} ${inputs.remoteData} ${inputs.config}" }
                    WifiState.Connected(
                        status = inputs.status,
                        propertyPresentations = wifiPropertyPresentationProvider(
                            enabledProperties = inputs.config.enabledProperties,
                            enabledIpSettings = inputs.config::enabledIpSettings,
                            remoteNetworkData = inputs.remoteData
                        )
                    )
                }
            }
        }
        .logOnCancellation("wifiState")
        .stateIn(scope, SharingStarted.WhileSubscribed(), WifiState.Disconnected)

    override fun onLocationAccessChanged() {
        scope.launch { locationAccessChanged.emit(Unit) }
    }
}

private data class DisplayInputs(val status: WifiStatus, val config: WidgetConfig, val remoteData: RemoteNetworkData)
