package com.w2sv.widget.rendering

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.w2sv.common.di.qualifier.AppDefaultScope
import com.w2sv.widget.ui.WifiGlanceWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import slimber.log.e

/**
 * Coordinates render-only updates without triggering remote-data recovery.
 *
 * Glance's [WifiGlanceWidget] does not necessarily restart `provideGlance()` when
 * `updateAll()` is called on an active session. Therefore, render requests both
 * notify active compositions through [revisions] and call `updateAll()` to ensure
 * inactive widget sessions are updated.
 *
 * Requests are conflated, since only the latest presentation state matters.
 */
@Singleton
internal class WidgetRenderCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    @AppDefaultScope scope: CoroutineScope
) {
    val revisions: StateFlow<Long>
        field = MutableStateFlow(0L)

    init {
        // A StateFlow retains a request made before this collector starts and conflates bursts.
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            revisions.filter { it > 0L }.collect {
                try {
                    WifiGlanceWidget().updateAll(context)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    // An update failure must not terminate delivery of later requests.
                    e { "Widget render failed: error=$exception" }
                }
            }
        }
    }

    /** Signals that installed widgets should render their latest available data. */
    fun requestRender() {
        revisions.update { it + 1L }
    }
}
