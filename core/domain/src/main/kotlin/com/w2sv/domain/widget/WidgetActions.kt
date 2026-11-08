package com.w2sv.domain.widget

interface WidgetActions {
    /**
     * Requests launcher pinning with a preview. Returns whether the platform accepted the request;
     * the user may still dismiss the confirmation.
     */
    suspend fun pin(): Boolean

    /**
     * Asks the provider to enqueue immediate refresh work. Unlike [requestUpdate], this does not
     * depend on the widget update callback finding missing remote data.
     */
    fun refresh()

    /** Requests an asynchronous Glance render from current data without scheduling refresh work. */
    fun render()

    /**
     * Sends the Android widget update callback for [appWidgetId]. Glance updates the widget, and
     * the provider schedules recovery only if required remote data is missing and refresh is
     * currently possible. Unlike [refresh], this does not enqueue immediate refresh work.
     */
    fun requestUpdate(appWidgetId: Int)

    /**
     * Called after configuration persistence succeeds. Updates periodic scheduling for installed
     * widgets when refresh settings change. Every commit requests an immediate render; only changed
     * remote-data requirements request an immediate refresh. RemoteDataSynchronizer owns
     * cache synchronization; RemoteDataWidgetObserver owns change-driven launcher rendering.
     */
    fun applyConfigurationChange(previous: WidgetConfig, current: WidgetConfig)
}
