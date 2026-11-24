package com.w2sv.widget.ui.state

import android.content.Context
import com.w2sv.core.resources.R
import com.w2sv.domain.location.LocationPermissionLevel
import com.w2sv.domain.location.locationPermissionLevel
import com.w2sv.domain.networking.NetworkAvailabilitySource
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.WifiStatusSource
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.WifiPropertyResolutionError
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentationProvider
import com.w2sv.widget.rendering.WidgetRenderCoordinator
import com.w2sv.widget.ui.theme.renderColors
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.TimeSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import slimber.log.d
import slimber.log.e

/** Reads config, route, cache, and Wi-Fi once per pass; property resolution never fetches remote data. */
@Singleton
internal class WidgetPresentationStateProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configSource: WidgetConfigSource,
    private val wifiStatusSource: WifiStatusSource,
    private val remoteData: RemoteNetworkDataRepository,
    private val availabilitySource: NetworkAvailabilitySource,
    private val presentations: WifiPropertyPresentationProvider,
    private val renderCoordinator: WidgetRenderCoordinator,
    private val clock: Clock
) {
    private val computationMutex = Mutex()

    @Volatile private var cached: Snapshot? = null

    val currentRevision: Long get() = renderCoordinator.revisions.value

    /** The revision belongs to the computation's start, even if requests arrive while it suspends. */
    data class Snapshot(val revision: Long, val state: WidgetPresentationState)

    /** Requests made during initial preparation are retained by the coordinator's StateFlow. */
    fun updates(afterRevision: Long): Flow<Snapshot> =
        renderCoordinator.revisions.filter { it > afterRevision }.mapNotNull { revision ->
            try {
                invoke(revision)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                // Keep the previous composition; the next request may recover.
                e { "Failed preparing widget presentation: revision=$revision, error=$exception" }
                null
            }
        }.distinctUntilChangedBy { it.revision }

    /** Only the latest successful snapshot is retained; failures and cancellation never cache. */
    suspend operator fun invoke(requestedRevision: Long = currentRevision): Snapshot {
        currentCoroutineContext().ensureActive()
        val started = TimeSource.Monotonic.markNow()
        d { "Widget presentation requested: revision=$requestedRevision" }
        cached?.takeIf { it.revision >= maxOf(requestedRevision, currentRevision) }?.let {
            d { "Widget presentation acquired: revision=${it.revision}, reused=true, duration=${started.elapsedNow()}" }
            return it
        }
        val waiting = TimeSource.Monotonic.markNow()
        return computationMutex.withLock {
            d { "Widget presentation lock acquired: requestedRevision=$requestedRevision, wait=${waiting.elapsedNow()}" }
            // Read the current revision again: delayed consumers must never compute older data
            // over a newer snapshot. Intermediate revisions can be skipped.
            val revision = maxOf(requestedRevision, currentRevision)
            cached?.takeIf { it.revision >= revision }?.let {
                d { "Widget presentation acquired: revision=${it.revision}, reused=true, duration=${started.elapsedNow()}" }
                return@withLock it
            }
            val computing = TimeSource.Monotonic.markNow()
            val snapshot = try {
                Snapshot(revision, computeState())
            } finally {
                d { "Widget presentation computation ended: revision=$revision, duration=${computing.elapsedNow()}" }
            }
            currentCoroutineContext().ensureActive()
            cached = snapshot
            d {
                "Widget presentation acquired: revision=$revision, reused=false, duration=${started.elapsedNow()}"
            }
            snapshot
        }
    }

    private suspend fun computeState(): WidgetPresentationState {
        val config = configSource.snapshot()
        val status = wifiStatusSource.snapshot()
        val colors = config.appearance.renderColors(context)
        return WidgetPresentationState(
            wifiState = if (status.isConnected) {
                WidgetWifiState.Connected(status, propertyRows(config))
            } else {
                WidgetWifiState.Disconnected(status)
            },
            colors = colors,
            fontSize = config.appearance.fontSize,
            valueAlignment = config.appearance.propertyValueAlignment,
            utilities = WidgetUtilities(config),
            renderTime = FormattedRenderTime(context, clock)
        )
    }

    private suspend fun propertyRows(config: WidgetConfig): List<WidgetPropertyRow> {
        val availability = availabilitySource()
        val remoteSnapshot = remoteData.widgetSnapshot(config.remoteDataConfig)
        val missingBackgroundAccess = context.locationPermissionLevel() == LocationPermissionLevel.PreciseForeground
        val nextRowIndex = mutableMapOf<WifiProperty, Int>()
        return presentations.withProperties(
            enabledProperties = config.enabledProperties,
            enabledIpSettings = config::enabledIpSettings,
            remoteNetworkData = remoteSnapshot,
            onUnresolved = availability::batterySaverPlaceholderFor
        ).map { (property, presentation) ->
            val index = nextRowIndex[property] ?: 0
            nextRowIndex[property] = index + 1
            // The shared resolver already distinguishes disabled services and preserves resolved values.
            val widgetPresentation = if (
                missingBackgroundAccess &&
                (property == WifiProperty.SSID || property == WifiProperty.BSSID) &&
                presentation.resolutionError == WifiPropertyResolutionError.NoLocationAccessPermission
            ) {
                presentation.copy(
                    value = context.getString(R.string.no_background_location_access),
                    resolutionError = WifiPropertyResolutionError.NoBackgroundLocationAccessPermission
                )
            } else {
                presentation
            }
            WidgetPropertyRow(property = property, presentation = widgetPresentation, propertyIndex = index)
        }
    }
}
