package com.w2sv.domain.widget

import kotlinx.coroutines.flow.Flow

interface WidgetActions {
    /** Emits when a widget pin requested by the app succeeds. */
    val pinSuccesses: Flow<Unit>

    fun pin(onFailure: () -> Unit)
    fun refresh()
    fun render()

    /**
     * Called after configuration persistence succeeds. Updates periodic scheduling only when
     * refresh settings change, and requests an immediate widget refresh for every commit,
     * including changes that do not publish new remote data. RemoteDataSynchronizer owns cache
     * synchronization; RemoteDataWidgetObserver owns change-driven launcher rendering.
     */
    fun applyConfigurationChange(previous: WidgetConfig, current: WidgetConfig)
}
