package com.w2sv.wifiwidget.ui.location

import com.w2sv.domain.location.LocationPermissionLevel
import com.w2sv.domain.widget.WidgetManager
import javax.inject.Inject
import javax.inject.Singleton

/** Process-wide recovery history shared by all activity hosts, without retaining their lifecycles. */
@Singleton
class WidgetLocationRecoveryObserver @Inject constructor(private val widgetManager: WidgetManager) {
    private var lastAvailability: LocationAccessAvailability? = null

    /** Serialize the comparison and render request so overlapping hosts cannot render twice. */
    @Synchronized
    internal fun observe(availability: LocationAccessAvailability) {
        val previous = lastAvailability
        lastAvailability = availability
        if (!availability.locationServicesEnabled || !availability.permissionLevel.hasPreciseAccess) return

        val recovered = previous == null || !previous.locationServicesEnabled || !previous.permissionLevel.hasPreciseAccess
        val backgroundUpgraded = previous?.permissionLevel == LocationPermissionLevel.PreciseForeground &&
            availability.permissionLevel == LocationPermissionLevel.PreciseBackground
        if (recovered || backgroundUpgraded) widgetManager.render()
    }
}
