package com.w2sv.domain.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import com.w2sv.androidutils.content.hasPermission
import com.w2sv.common.android.location.requiresBackgroundLocationPermission

/** Read current grants synchronously when lifecycle reconciliation precedes Compose state updates. */
// Lint misses the annotated common-module getter; the short-circuit guard still excludes pre-Q devices.
@SuppressLint("InlinedApi")
fun Context.locationPermissionLevel(): LocationPermissionLevel =
    LocationPermissionLevel.fromGrants(
        preciseGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION),
        approximateGranted = hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION),
        backgroundAccessAvailable =
            !requiresBackgroundLocationPermission || hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    )

/**
 * Effective Android location permission available to WiFi Widget, independently of system-wide
 * location services. [PreciseBackground] also covers Android versions where a separate background
 * permission grant is unnecessary.
 */
enum class LocationPermissionLevel {
    None,
    ApproximateOnly,
    PreciseForeground,
    PreciseBackground;

    val isPreciseForeground: Boolean
        get() = this == PreciseForeground

    val hasPreciseAccess: Boolean
        get() = this == PreciseForeground || this == PreciseBackground

    companion object {
        fun fromGrants(
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
