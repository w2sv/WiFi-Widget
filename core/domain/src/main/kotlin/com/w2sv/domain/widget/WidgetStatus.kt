package com.w2sv.domain.widget

import kotlinx.coroutines.flow.Flow

/**
 * Exposes installed Wi-Fi widget state and successful in-app pin results to consumers.
 *
 * Installation state is read when requested rather than retained here, so it reflects widgets
 * added or removed by Android widget hosts independently of an in-app pin request.
 */
interface WidgetStatus {
    /** Emits when a widget pin requested by the app succeeds. */
    val pinSuccesses: Flow<Unit>

    /** IDs of currently installed Wi-Fi widgets, queried on each access. */
    val ids: IntArray

    /** Whether any Android widget host currently has an installed Wi-Fi widget. */
    val hasInstalledWidgets: Boolean
}
