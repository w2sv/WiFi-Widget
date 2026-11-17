package com.w2sv.domain.location

import kotlinx.coroutines.flow.Flow

interface IsLocationEnabled {
    /** Returns whether system location services are currently enabled. */
    operator fun invoke(): Boolean

    /** Emits the current state and subsequent changes while collected. */
    val flow: Flow<Boolean>
}
