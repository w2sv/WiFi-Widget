package com.w2sv.wifiwidget.ui.location

/**
 * Effective Android location permission available to WiFi Widget, independently of system-wide
 * location services. [PreciseBackground] also covers Android versions where a separate background
 * permission grant is unnecessary.
 */
internal enum class LocationPermissionLevel {
    None,
    ApproximateOnly,
    PreciseForeground,
    PreciseBackground;

    val isPreciseForeground: Boolean
        get() = this == PreciseForeground

    val hasPreciseAccess: Boolean
        get() = this == PreciseForeground || this == PreciseBackground

    companion object {
        internal fun fromGrants(
            preciseGranted: Boolean,
            approximateGranted: Boolean,
            backgroundAccessAvailable: Boolean
        ): LocationPermissionLevel =
            when {
                preciseGranted && backgroundAccessAvailable -> PreciseBackground
                preciseGranted -> PreciseForeground
                approximateGranted -> ApproximateOnly
                else -> None
            }
    }
}
