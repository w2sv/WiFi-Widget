package com.w2sv.wifiwidget.ui.location

import com.w2sv.domain.widget.WidgetActions
import javax.inject.Inject
import javax.inject.Singleton

/** Process-wide recovery history shared by all activity hosts, without retaining their lifecycles. */
@Singleton
class WidgetLocationRecoveryObserver @Inject constructor(private val widgetActions: WidgetActions) {
    private var lastAvailability: LocationAccessAvailability? = null

    /** Serialize both the comparison and refresh request so overlapping hosts cannot refresh twice. */
    @Synchronized
    internal fun observe(availability: LocationAccessAvailability) {
        val previous = lastAvailability
        lastAvailability = availability
        if (!availability.locationServicesEnabled || !availability.permissionLevel.hasPreciseAccess) return

        val recovered = previous == null || !previous.locationServicesEnabled || !previous.permissionLevel.hasPreciseAccess
        val backgroundUpgraded = previous?.permissionLevel == LocationPermissionLevel.PreciseForeground &&
            availability.permissionLevel == LocationPermissionLevel.PreciseBackground
        if (recovered || backgroundUpgraded) widgetActions.refresh()
    }
}
