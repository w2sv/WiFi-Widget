package com.w2sv.wifiwidget.ui.screen.locationaccess

import kotlinx.serialization.Serializable

/** Why Location Access was opened. [RefreshWidget] refreshes the widget after a successful grant. */
@Serializable
sealed interface LocationAccessScreenScenario {
    @Serializable
    data object Onboarding : LocationAccessScreenScenario

    @Serializable
    data object ManagePermissions : LocationAccessScreenScenario

    @Serializable
    @JvmInline
    value class EnableWidgetProperty(val propertyId: Int) : LocationAccessScreenScenario

    @Serializable
    data object RefreshWidget : LocationAccessScreenScenario
}
