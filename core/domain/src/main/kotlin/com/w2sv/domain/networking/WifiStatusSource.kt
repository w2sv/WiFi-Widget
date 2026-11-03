package com.w2sv.domain.networking

import com.w2sv.domain.networking.WifiStatus
import kotlinx.coroutines.flow.Flow

/**
 * Provides synchronous snapshots and reactive updates of Wi-Fi connectivity, including
 * default-egress changes relevant to remotely fetched network data. Callers that need a
 * one-time status can avoid the callback lifetime owned by a flow subscription.
 *
 * Both flows are cold: collecting one registers its callbacks, and cancelling collection
 * unregisters them. Default egress has its own stream because it can change while Wi-Fi
 * status stays the same.
 */
interface WifiStatusSource {

    /** Returns the current Wi-Fi status without registering connectivity callbacks. */
    fun snapshot(): WifiStatus

    /**
     * Returns the selected connected Wi-Fi network's process-local identity, or null when Wi-Fi
     * is absent or ambiguous. Equal status values do not imply equal network identities.
     */
    fun connectedWifiIdentity(): Long?

    /**
     * Emits the initial Wi-Fi status and subsequent connectivity updates.
     * Consecutive emissions may be identical because local Wi-Fi properties can change
     * without changing the connection state.
     */
    val status: Flow<WifiStatus>

    /**
     * Signals changes to the default network's identity or INTERNET capability, including
     * transitions to and from having no default network. The initial state is the baseline;
     * unchanged initial callbacks do not emit.
     */
    val defaultEgressChanges: Flow<Unit>
}
