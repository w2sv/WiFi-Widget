package com.w2sv.wifiwidget.ui.screen.locationaccess

import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.location.LocationPermissionLevel
import com.w2sv.wifiwidget.ui.screen.locationaccess.components.LocationAccessActionPanelPresentation
import com.w2sv.wifiwidget.ui.screen.locationaccess.components.LocationAccessActionPanelPresentation.Action
import com.w2sv.wifiwidget.ui.screen.locationaccess.components.LocationAccessActionPanelPresentation.ActionKind
import com.w2sv.wifiwidget.ui.screen.locationaccess.components.LocationAccessPresentationStatus
import com.w2sv.wifiwidget.ui.screen.locationaccess.state.LocationAccessAvailability
import com.w2sv.wifiwidget.ui.screen.locationaccess.state.LocationAccessScreenState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationAccessActionPanelPresentationTest {
    private val state = LocationAccessScreenState(
        isOnboarding = true,
        availability = LocationAccessAvailability(PreciseBackground, locationServicesEnabled = true),
        displayedPermissionLevel = None,
        foregroundRequestOpensSettings = false,
        actionsEnabled = false
    )

    @Test
    fun `pending interaction retains actions while status reflects newly granted access`() {
        val presentation = LocationAccessActionPanelPresentation.from(state)

        assertEquals(LocationAccessPresentationStatus.BackgroundAccessAvailable, presentation.status)
        assertEquals(Action(R.string.enable_location_access, ActionKind.RequestForeground), presentation.primaryAction)
        assertEquals(Action(R.string.continue_without_location_access, ActionKind.Continue), presentation.secondaryAction)
        assertFalse(presentation.actionsEnabled)
        assertFalse(presentation.showAppSettingsAction)
        assertTrue(presentation.showOnboardingHint)
    }

    @Test
    fun `foreground action label follows the resolved request destination`() {
        val presentation = LocationAccessActionPanelPresentation.from(state.copy(foregroundRequestOpensSettings = true))

        assertEquals(Action(R.string.go_to_app_settings, ActionKind.RequestForeground), presentation.primaryAction)
    }

    @Test
    fun `management offers background access and settings before switching to done`() {
        val managementState = state.copy(
            isOnboarding = false,
            displayedPermissionLevel = LocationPermissionLevel.PreciseForeground,
            actionsEnabled = true
        )
        val foreground = LocationAccessActionPanelPresentation.from(managementState)

        assertEquals(Action(R.string.enable_background_updates, ActionKind.RequestBackground), foreground.primaryAction)
        assertEquals(Action(R.string.done, ActionKind.Continue), foreground.secondaryAction)
        assertTrue(foreground.showAppSettingsAction)
        assertFalse(foreground.showOnboardingHint)
        assertTrue(foreground.actionsEnabled)

        val backgroundState = managementState.copy(displayedPermissionLevel = LocationPermissionLevel.PreciseBackground)
        val background = LocationAccessActionPanelPresentation.from(backgroundState)

        assertEquals(Action(R.string.done, ActionKind.Continue), background.primaryAction)
        assertEquals(Action(R.string.manage_in_android_settings, ActionKind.OpenAppSettings), background.secondaryAction)
        assertFalse(background.showAppSettingsAction)

        val onboarding = LocationAccessActionPanelPresentation.from(backgroundState.copy(isOnboarding = true))

        assertEquals(Action(R.string.continue_to_wifi_widget, ActionKind.Continue), onboarding.primaryAction)
        assertNull(onboarding.secondaryAction)
    }
}
