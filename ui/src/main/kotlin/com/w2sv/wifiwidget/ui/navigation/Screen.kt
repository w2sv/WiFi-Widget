package com.w2sv.wifiwidget.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessScreenScenario
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessScreenScenario.Onboarding
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    object Home : Screen

    @Serializable
    @JvmInline
    value class LocationAccess(val scenario: LocationAccessScreenScenario) : Screen

    @Serializable
    object WidgetConfiguration : Screen

    companion object {
        val LocationAccessOnboarding: Screen = LocationAccess(Onboarding)
    }
}
