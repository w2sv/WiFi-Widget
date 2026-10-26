package com.w2sv.domain.location

/** Reads whether system-wide location services are currently enabled. */
fun interface IsLocationEnabled {
    operator fun invoke(): Boolean
}
