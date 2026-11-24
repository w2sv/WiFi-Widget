package com.w2sv.widget.rendering

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.w2sv.common.di.qualifier.AppDefaultScope
import com.w2sv.kotlinutils.coroutines.runOrHandle
import com.w2sv.widget.ui.WifiGlanceWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.TimeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import slimber.log.d
import slimber.log.e

/**
 * Owns presentation revisions and conflated Glance dispatch, without fetching or scheduling recovery.
 * Active compositions observe [revisions]; updateAll also starts inactive Glance sessions.
 */
@Singleton
internal class WidgetRenderCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    @AppDefaultScope scope: CoroutineScope
) {
    val revisions: StateFlow<Long>
        field = MutableStateFlow(0L)

    private val dispatchRequests = MutableStateFlow(0L)
    private val dispatchMutex = Mutex()
    private var dispatchedRevision = 0L
    private val initializationLock = Any()

    private val initializations = mutableSetOf<PresentationInitialization>()

    init {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            dispatchRequests.filter { it > 0L }.collect {
                runOrHandle(onFailure = { exception ->
                    // Failed dispatch must not terminate delivery of later requests.
                    e { "Widget render failed: error=$exception" }
                }) { dispatchPending() }
            }
        }
    }

    /** Invalidates data and signals both active and inactive widget sessions. */
    fun requestRender() {
        val revision = revisions.updateAndGet { it + 1L }
        dispatchRequests.updateAndGet { maxOf(it, revision) }
        d { "Widget render requested: revision=$revision" }
    }

    /** System callbacks already own Glance dispatch; notify active compositions without another updateAll. */
    fun invalidatePresentation(): Long =
        revisions.updateAndGet { it + 1L }.also { revision ->
            d { "Widget presentation invalidated: revision=$revision" }
        }

    /**
     * Only overlapping initial preparations can establish that sharing is fresh. Once preparation
     * finishes, a separately initialized session needs a new baseline, even if other compositions
     * remain active or a newer revision was computed while this session was absent.
     * This invalidates presentation only; it neither dispatches updateAll nor schedules recovery.
     */
    suspend fun <T> withPresentationInitialization(block: suspend () -> T): T {
        val initialization = synchronized(initializationLock) {
            if (initializations.none { it.revision == revisions.value }) {
                val revision = revisions.updateAndGet { it + 1L }
                d { "Widget presentation session baseline invalidated: revision=$revision" }
            }
            PresentationInitialization(revisions.value).also(initializations::add)
        }
        try {
            return block()
        } finally {
            synchronized(initializationLock) {
                // Identity-based removal cannot remove a replacement's initialization.
                initializations.remove(initialization)
            }
        }
    }

    private class PresentationInitialization(val revision: Long)

    /**
     * Awaits serialized Glance dispatch, skipping revisions already dispatched. Failures leave
     * revisions pending. Completion neither confirms launcher display nor guarantees delivery
     * across process death.
     */
    suspend fun dispatchPending() =
        dispatchMutex.withLock {
            val revision = dispatchRequests.value
            if (revision <= dispatchedRevision) return@withLock
            val started = TimeSource.Monotonic.markNow()
            d { "Widget updateAll started: revision=$revision" }
            try {
                WifiGlanceWidget().updateAll(context)
                dispatchedRevision = revision
            } finally {
                d { "Widget updateAll finished: revision=$revision, duration=${started.elapsedNow()}" }
            }
        }
}
