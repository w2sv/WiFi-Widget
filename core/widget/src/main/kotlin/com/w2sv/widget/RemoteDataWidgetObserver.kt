package com.w2sv.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import com.w2sv.androidutils.appwidget.appWidgetIds
import com.w2sv.common.di.qualifier.AppDefaultScope
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.widget.WidgetActions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import slimber.log.e

/**
 * Observes published remote-data changes and requests launcher-widget rendering.
 *
 * This application-scoped observer handles changes independently of the component that
 * initiated them. Its subscription is established during application startup, before
 * initialization returns.
 *
 * The initial StateFlow emission is skipped because it represents the current baseline rather
 * than a new change. Android widget callbacks and recovery workers provide additional
 * opportunities to restore data and rendering after process recreation; recovery workers also
 * render their own published changes before work completes.
 *
 * Rendering is requested only while widget instances are installed. The observer neither
 * fetches nor modifies remote data.
 */
@Singleton
class RemoteDataWidgetObserver @Inject constructor(
    private val remoteNetworkDataRepository: RemoteNetworkDataRepository,
    private val widgetActions: WidgetActions,
    private val appWidgetManager: AppWidgetManager,
    @ApplicationContext private val context: Context,
    @AppDefaultScope private val scope: CoroutineScope
) {
    private var initialized = false

    /** Starts exactly one subscription, including when called repeatedly during startup. */
    @Synchronized
    fun initialize() {
        if (initialized) return
        initialized = true
        // UNDISPATCHED consumes StateFlow's baseline before callers can publish a new value.
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            remoteNetworkDataRepository.data.drop(1).collect {
                try {
                    if (appWidgetManager.appWidgetIds<WifiWidgetProvider>(context).isNotEmpty()) {
                        widgetActions.render()
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    e { "Failed requesting widget render after remote-data change: error=$exception" }
                }
            }
        }
    }
}
