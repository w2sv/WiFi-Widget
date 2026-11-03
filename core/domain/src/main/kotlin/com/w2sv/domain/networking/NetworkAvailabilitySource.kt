package com.w2sv.domain.networking

/** Reads one network-availability snapshot without retaining connectivity callbacks. */
fun interface NetworkAvailabilitySource {

    /** Returns one observation for a widget rendering pass or default-route event. */
    operator fun invoke(): NetworkAvailabilitySnapshot
}
