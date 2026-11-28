package com.w2sv.wifiwidget.ui.screen.locationaccess

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.w2sv.domain.location.LocationPermissionLevel
import com.w2sv.wifiwidget.ui.location.LocationAccessAvailability
import com.w2sv.wifiwidget.ui.screen.locationaccess.state.LocationAccessScreenActions
import com.w2sv.wifiwidget.ui.screen.locationaccess.state.LocationAccessScreenState
import com.w2sv.wifiwidget.ui.util.PreviewOf

private const val LANDSCAPE_DEVICE = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420"

@Preview(name = "Onboarding - No access", showSystemUi = true)
@Preview(name = "Landscape - Onboarding - No access", device = LANDSCAPE_DEVICE, showSystemUi = true)
@Composable
private fun OnboardingNoAccessPrev() {
    LocationAccessScreenPreview(previewState(LocationPermissionLevel.None, isOnboarding = true))
}

@Preview(name = "Onboarding - Approximate only", showSystemUi = true)
@Composable
private fun OnboardingApproximateAccessPrev() {
    LocationAccessScreenPreview(previewState(LocationPermissionLevel.ApproximateOnly, isOnboarding = true))
}

@Preview(name = "Onboarding - Precise foreground", showSystemUi = true)
@Preview(name = "Landscape - Onboarding - Precise foreground", device = LANDSCAPE_DEVICE, showSystemUi = true)
@Composable
private fun OnboardingForegroundAccessPrev() {
    LocationAccessScreenPreview(previewState(LocationPermissionLevel.PreciseForeground, isOnboarding = true))
}

@Preview(name = "Management - Precise foreground", showSystemUi = true)
@Preview(name = "Landscape - Management - Precise foreground", device = LANDSCAPE_DEVICE, showSystemUi = true)
@Composable
private fun ManagementForegroundAccessPrev() {
    LocationAccessScreenPreview(previewState(LocationPermissionLevel.PreciseForeground))
}

@Preview(name = "Management - Background access", showSystemUi = true)
@Composable
private fun ManagementBackgroundAccessPrev() {
    LocationAccessScreenPreview(previewState(LocationPermissionLevel.PreciseBackground))
}

@Preview(name = "Management - Location services off", showSystemUi = true)
@Preview(name = "Landscape - Management - Location services off", device = LANDSCAPE_DEVICE, showSystemUi = true)
@Composable
private fun ManagementLocationServicesOffPrev() {
    LocationAccessScreenPreview(previewState(LocationPermissionLevel.PreciseBackground, locationServicesEnabled = false))
}

@Preview(name = "Onboarding - Foreground request pending", showSystemUi = true)
@Composable
private fun OnboardingForegroundRequestPendingPrev() {
    LocationAccessScreenPreview(
        previewState(
            permissionLevel = LocationPermissionLevel.PreciseForeground,
            isOnboarding = true,
            displayedPermissionLevel = LocationPermissionLevel.None,
            actionsEnabled = false
        )
    )
}

@Composable
private fun LocationAccessScreenPreview(state: LocationAccessScreenState) {
    PreviewOf {
        LocationAccessScreen(state = state, actions = previewActions)
    }
}

private fun previewState(
    permissionLevel: LocationPermissionLevel,
    isOnboarding: Boolean = false,
    locationServicesEnabled: Boolean = true,
    displayedPermissionLevel: LocationPermissionLevel = permissionLevel,
    foregroundRequestOpensSettings: Boolean = permissionLevel == LocationPermissionLevel.ApproximateOnly,
    actionsEnabled: Boolean = true
): LocationAccessScreenState =
    LocationAccessScreenState(
        isOnboarding = isOnboarding,
        availability = LocationAccessAvailability(permissionLevel, locationServicesEnabled),
        displayedPermissionLevel = displayedPermissionLevel,
        foregroundRequestOpensSettings = foregroundRequestOpensSettings,
        actionsEnabled = actionsEnabled
    )

private val previewActions = LocationAccessScreenActions(
    onBack = {},
    onRequestForeground = {},
    onRequestBackground = {},
    onOpenAppSettings = {},
    onOpenLocationSettings = {},
    onContinue = {}
)
