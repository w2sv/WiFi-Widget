package com.w2sv.domain.widget

interface WidgetActions {
    fun pin(onFailure: () -> Unit)
    fun refresh()
    fun render()

    /** Requests the provider's full update lifecycle for a newly configured widget. */
    fun requestUpdate(appWidgetId: Int)

    /**
     * Called after configuration persistence succeeds. Updates periodic scheduling only when
     * refresh settings change, and requests an immediate widget refresh for every commit,
     * including changes that do not publish new remote data. RemoteDataSynchronizer owns cache
     * synchronization; RemoteDataWidgetObserver owns change-driven launcher rendering.
     */
    fun applyConfigurationChange(previous: WidgetConfig, current: WidgetConfig)
}
