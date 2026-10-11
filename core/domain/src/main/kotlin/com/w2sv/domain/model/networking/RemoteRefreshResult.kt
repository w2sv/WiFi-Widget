package com.w2sv.domain.model.networking

/**
 * Reports whether a remote-data refresh satisfied the enabled requirements for the current
 * default egress.
 * Recovery work uses [Complete] to stop retrying and retries [Incomplete] or [NetworkUnavailable].
 */
enum class RemoteRefreshResult {
    /** All enabled remote-data requirements are sufficiently initialized for recovery to stop. */
    Complete,

    /** The route is known, but an enabled requirement still needs initialization or an initial attempt. */
    Incomplete,

    /** The default egress context is unknown or changed while requests were in flight. */
    NetworkUnavailable
}
