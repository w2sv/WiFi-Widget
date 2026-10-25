package com.w2sv.widget

import com.w2sv.common.di.qualifier.AppDefaultScope
import com.w2sv.domain.networking.NetworkAvailabilitySnapshot
import com.w2sv.domain.networking.NetworkAvailabilitySource
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.networking.WifiStatusSource
import com.w2sv.domain.widget.WidgetActions
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.domain.widget.WidgetStatus
import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import slimber.log.e

/**
 * Observes published remote-data and connected Wi-Fi changes for launcher widgets.
 *
 * This application-scoped observer handles changes independently of the component that
 * initiated them. Its remote-data subscription is established during application startup, before
 * initialization returns.
 *
 * The initial StateFlow emission is skipped because it represents the current baseline rather
 * than a new change. Android widget callbacks and recovery workers provide additional
 * opportunities to restore data and rendering after process recreation; recovery workers also
 * render their own published changes before work completes.
 *
 * The Wi-Fi and default-route subscriptions run only while this process has installed widgets;
 * the widget provider starts or stops them as widgets are added or removed. A killed process cannot receive
 * callbacks, so later widget renders still reconcile the current Wi-Fi identity independently.
 * Default-route events reconcile repository state and may enqueue recovery; HTTP remains in the
 * worker.
 */
@Singleton
class RemoteDataWidgetObserver @Inject internal constructor(
    private val remoteNetworkDataRepository: RemoteNetworkDataRepository,
    private val wifiStatusSource: WifiStatusSource,
    private val networkAvailabilitySource: NetworkAvailabilitySource,
    private val widgetConfigSource: WidgetConfigSource,
    private val workScheduler: WifiWidgetWorkScheduler,
    private val widgetActions: WidgetActions,
    private val widgetStatus: WidgetStatus,
    @AppDefaultScope private val scope: CoroutineScope
) {
    private var initialized = false
    private var wifiObservationJob: Job? = null
    private var defaultEgressObservationJob: Job? = null

    /** Starts the remote-data subscription once and observes networks while widgets are installed. */
    @Synchronized
    fun initialize() {
        if (initialized) return
        initialized = true
        // UNDISPATCHED consumes StateFlow's baseline before callers can publish a new value.
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            remoteNetworkDataRepository.data.drop(1).collect {
                try {
                    if (widgetStatus.hasInstalledWidgets) {
                        widgetActions.render()
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    e { "Failed requesting widget render after remote-data change: error=$exception" }
                }
            }
        }
        if (widgetStatus.hasInstalledWidgets) {
            startNetworkObservation()
        }
    }

    /** Starts network observation after a widget is added or restored, without duplicate collectors. */
    @Synchronized
    fun onWidgetsAvailable() {
        if (!initialized) initialize()
        startNetworkObservation()
    }

    /** Releases both network callbacks after the last widget is removed. */
    @Synchronized
    fun onLastWidgetRemoved() {
        wifiObservationJob?.cancel()
        wifiObservationJob = null
        defaultEgressObservationJob?.cancel()
        defaultEgressObservationJob = null
    }

    private fun startNetworkObservation() {
        if (wifiObservationJob?.isActive != true) {
            wifiObservationJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                observeWifiConnections()
            }
        }
        if (defaultEgressObservationJob?.isActive != true) {
            defaultEgressObservationJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                observeDefaultEgress()
            }
        }
    }

    /** Tracks connection identity as well as status, since either can change the displayed rows. */
    private suspend fun observeWifiConnections() {
        var previous: ObservedWifiConnection? = null
        // The first emission establishes a baseline; later callback events can represent
        // a new Network even while Wi-Fi status remains ConnectedInactive.
        wifiStatusSource.status.collect { status ->
            try {
                val current = ObservedWifiConnection(status, wifiStatusSource.connectedWifiIdentity())
                if (previous != null && previous != current && widgetStatus.hasInstalledWidgets) {
                    if (status.isConnected) scheduleRecoveryAfterWifiChange()
                    widgetActions.render()
                }
                previous = current
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                e { "Failed requesting widget render after Wi-Fi change: error=$exception" }
            }
        }
    }

    /** A VPN default can remain unchanged while Wi-Fi reconnects and makes widget HTTP eligible. */
    private suspend fun scheduleRecoveryAfterWifiChange() {
        if (!networkAvailabilitySource().canRefreshWidgetRemoteData) return

        val config = widgetConfigSource.snapshot().remoteDataConfig
        if (remoteNetworkDataRepository.needsRecovery(config)) {
            workScheduler.enqueueRecovery()
        }
    }

    /** Reconciles default-route changes even when the connected Wi-Fi identity stays the same. */
    private suspend fun observeDefaultEgress() {
        var previous: ObservedDefaultEgress? = null
        wifiStatusSource.defaultEgressChanges.collect {
            try {
                val availability = networkAvailabilitySource()
                val current = ObservedDefaultEgress(
                    availability.defaultNetworkIdentity,
                    availability.hasUsableDefaultNetwork,
                    availability.isDirectWifiDefaultNetwork,
                    availability.isVpnDefaultNetwork
                )
                if (current == previous || !widgetStatus.hasInstalledWidgets) {
                    return@collect
                }

                reconcileDefaultEgress(availability)
                previous = current
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                e { "Failed reconciling widget after default-route change: error=$exception" }
            }
        }
    }

    /** Schedules recovery only for widget-eligible egress and renders when reconciliation publishes no data change. */
    private suspend fun reconcileDefaultEgress(availability: NetworkAvailabilitySnapshot) {
        val config = widgetConfigSource.snapshot().remoteDataConfig
        val before = remoteNetworkDataRepository.data.value
        val needsRecovery = remoteNetworkDataRepository.needsRecovery(config)
        if (availability.canRefreshWidgetRemoteData && needsRecovery) {
            workScheduler.enqueueRecovery()
        }
        // Publication already requests a render through the data collector. When it stays equal,
        // availability or local routing may still change rows.
        if (before == remoteNetworkDataRepository.data.value) {
            widgetActions.render()
        }
    }
}

/** A Wi-Fi observation includes identity because two connections can share the same status. */
private data class ObservedWifiConnection(val status: WifiStatus, val identity: Long?)

/** Tracks the default route's identity, usability, and transport-dependent presentation eligibility. */
private data class ObservedDefaultEgress(val identity: Long?, val isUsable: Boolean, val isDirectWifi: Boolean, val isVpn: Boolean)
