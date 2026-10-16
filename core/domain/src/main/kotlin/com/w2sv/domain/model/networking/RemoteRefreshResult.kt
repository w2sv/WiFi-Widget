package com.w2sv.domain.model.networking

enum class RemoteRefreshResult {
    /** All enabled remote-data requirements are sufficiently initialized for recovery to stop. */
    Complete,

    /** The route is known, but an enabled requirement still needs initialization or an initial attempt. */
    Incomplete,

    /** The default egress context is unknown or changed while requests were in flight. */
    NetworkUnavailable
}
