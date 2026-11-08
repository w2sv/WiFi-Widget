package com.w2sv.widget.ui.state

import android.content.Context
import com.w2sv.androidutils.graphics.getAlphaSetColor
import com.w2sv.domain.networking.NetworkAvailabilitySource
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.WifiStatusSource
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentationProvider
import com.w2sv.widget.rendering.WidgetRenderCoordinator
import com.w2sv.widget.ui.theme.resolve
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import slimber.log.e

/** Reads config, route, cache, and Wi-Fi once per pass; property resolution never fetches remote data. */
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
    val currentRevision: Long get() = renderCoordinator.revisions.value

    /** Requests made during initial preparation are retained; the initial snapshot is not prepared twice. */
    fun updates(afterRevision: Long): Flow<WidgetPresentationState> =
        renderCoordinator.revisions.filter { it > afterRevision }.mapNotNull {
            try {
                invoke()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                // Keep the previous composition; the next request may recover.
                e { "Failed preparing widget presentation: error=$exception" }
                null
            }
        }

    suspend operator fun invoke(): WidgetPresentationState {
        val config = configSource.snapshot()
        val status = wifiStatusSource.snapshot()
        val colors = config.appearance.coloring.resolve(context)
        return WidgetPresentationState(
            wifiState = if (status.isConnected) {
                WidgetWifiState.Connected(status, propertyRows(config))
            } else {
                WidgetWifiState.Disconnected(status)
            },
            colors = colors,
            backgroundColor = getAlphaSetColor(colors.background, config.appearance.backgroundOpacity),
            fontSize = config.appearance.fontSize,
            valueAlignment = config.appearance.propertyValueAlignment,
            utilities = WidgetUtilities(config),
            renderTime = FormattedRenderTime(context, clock)
        )
    }

    private suspend fun propertyRows(config: WidgetConfig): List<WidgetPropertyRow> {
        val availability = availabilitySource()
        val remoteSnapshot = remoteData.widgetSnapshot(config.remoteDataConfig)
        val nextRowIndex = mutableMapOf<WifiProperty, Int>()
        return presentations.withProperties(
            enabledProperties = config.enabledProperties,
            enabledIpSettings = config::enabledIpSettings,
            remoteNetworkData = remoteSnapshot,
            onUnresolved = availability::batterySaverPlaceholderFor
        ).map { (property, presentation) ->
            val index = nextRowIndex[property] ?: 0
            nextRowIndex[property] = index + 1
            WidgetPropertyRow(property = property, presentation = presentation, propertyIndex = index)
        }
    }
}
