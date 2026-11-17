package com.w2sv.wifiwidget.ui.screen.locationaccess.components

import androidx.annotation.StringRes
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.screen.locationaccess.state.LocationAccessAvailability

/** Informational presentation derived from [LocationAccessAvailability], not permission-flow state. */
internal enum class LocationAccessPresentationStatus(
    @StringRes val title: Int,
    @StringRes val description: Int? = null,
    val positive: Boolean = false,
    val opensLocationSettings: Boolean = false
) {
    PreciseRequired(R.string.precise_location_required, R.string.precise_location_required_explanation),
    ServicesOff(R.string.location_services_off, R.string.location_services_off_explanation, opensLocationSettings = true),

    /** Effective background access is available; a separate Android permission may not be required. */
    BackgroundAccessAvailable(R.string.background_location_access_available, positive = true),
    PreciseAccessAvailable(R.string.precise_location_access_granted, positive = true);

    companion object {
        fun from(availability: LocationAccessAvailability): LocationAccessPresentationStatus? =
            when {
                availability.permissionLevel == ApproximateOnly -> PreciseRequired
                availability.permissionLevel == None -> null
                !availability.locationServicesEnabled -> ServicesOff
                availability.permissionLevel == PreciseBackground -> BackgroundAccessAvailable
                else -> PreciseAccessAvailable
            }
    }
}
