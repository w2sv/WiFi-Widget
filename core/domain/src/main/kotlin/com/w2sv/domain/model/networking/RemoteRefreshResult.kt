package com.w2sv.domain.model.networking

enum class RemoteRefreshResult {
    /** All enabled groups have a successful value from this or an earlier valid refresh. */
    Complete,

    /** The route is known, but a required group has never fetched successfully for it. */
    Incomplete,

    /** The default egress context is unknown or changed while requests were in flight. */
    NetworkUnavailable
}
