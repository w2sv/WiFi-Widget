package com.w2sv.wifiwidget.ui.screen.locationaccess

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.wifiwidget.ui.location.LocationAccessAvailability
import com.w2sv.wifiwidget.ui.location.LocationPermissionLevel
import com.w2sv.wifiwidget.ui.location.capability.access.LocalLocationAccessCapability
import com.w2sv.wifiwidget.ui.location.capability.access.LocationAccessCapability
import com.w2sv.wifiwidget.ui.screen.locationaccess.components.BackgroundLocationDisclosure
import com.w2sv.wifiwidget.ui.screen.locationaccess.flow.LocationAccessEffect
import com.w2sv.wifiwidget.ui.screen.locationaccess.flow.LocationAccessEvent
import com.w2sv.wifiwidget.ui.screen.locationaccess.flow.LocationAccessFlowState
import com.w2sv.wifiwidget.ui.screen.locationaccess.flow.ObserveLocationPermissionRequests
import com.w2sv.wifiwidget.ui.screen.locationaccess.flow.rememberLocationAccessAvailability
import com.w2sv.wifiwidget.ui.screen.locationaccess.state.LocationAccessScreenActions
import com.w2sv.wifiwidget.ui.screen.locationaccess.state.LocationAccessScreenState

@Composable
fun LocationAccessRoute(
    scenario: LocationAccessScreenScenario,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    viewModel: LocationAccessScreenViewModel = hiltViewModel<LocationAccessScreenViewModel, LocationAccessScreenViewModel.Factory>(
        creationCallback = { factory -> factory.create(scenario) }
    )
) {
    val capability = LocalLocationAccessCapability.current
    val context = LocalContext.current
    val flowState by viewModel.flowState.collectAsStateWithLifecycle()
    val availability = rememberLocationAccessAvailability(context, capability, viewModel.flowState, viewModel::onEvent)

    ObserveLocationPermissionRequests(capability, availability, flowState, viewModel::onEvent)
    ObserveOnboardingCompletion(shouldComplete = scenario == Onboarding && flowState is LocationAccessFlowState.Completing) {
        viewModel.completeOnboarding(availability.permissionLevel.hasPreciseAccess)
        onContinue()
    }
    LocationAccessContent(
        scenario,
        capability,
        availability,
        flowState,
        viewModel::onEvent,
        onBack
    )
    if (flowState is LocationAccessFlowState.BackgroundDisclosure) {
        BackgroundLocationDisclosure(
            capability,
            viewModel::onEvent
        )
    }
}

@Composable
private fun ObserveOnboardingCompletion(shouldComplete: Boolean, onCompletedOnboarding: suspend () -> Unit) {
    val currentOnComplete by rememberUpdatedState(onCompletedOnboarding)

    LaunchedEffect(shouldComplete) {
        if (!shouldComplete) return@LaunchedEffect
        currentOnComplete()
    }
}

@Composable
private fun LocationAccessContent(
    scenario: LocationAccessScreenScenario,
    capability: LocationAccessCapability,
    availability: LocationAccessAvailability,
    flowState: LocationAccessFlowState?,
    onEvent: (LocationAccessEvent) -> LocationAccessEffect?,
    onBack: () -> Unit
) {
    val isOnboarding = scenario == Onboarding
    val displayedPermissionLevel = flowState?.displayedPermissionLevel ?: availability.permissionLevel
    val foregroundRequestOpensSettings = when (flowState) {
        is LocationAccessFlowState.RequestingForeground -> false
        is LocationAccessFlowState.ForegroundSettings -> true
        else -> displayedPermissionLevel == LocationPermissionLevel.ApproximateOnly || capability.foregroundRequestSuppressed
    }
    val state = LocationAccessScreenState(
        isOnboarding = isOnboarding,
        availability = availability,
        displayedPermissionLevel = displayedPermissionLevel,
        foregroundRequestOpensSettings = foregroundRequestOpensSettings,
        actionsEnabled = flowState == null
    )

    LocationAccessScreen(
        state = state,
        actions = LocationAccessScreenActions(
            onBack = onBack,
            onRequestForeground = {
                val effect = onEvent(
                    LocationAccessEvent.RequestForeground(state.foregroundRequestOpensSettings, state.displayedPermissionLevel)
                )
                capability.launchLocationAccessEffect(effect)
            },
            onRequestBackground = { onEvent(LocationAccessEvent.ShowBackgroundDisclosure(state.displayedPermissionLevel)) },
            onOpenAppSettings = capability::openAppSettings,
            onOpenLocationSettings = capability::openLocationSettings,
            onContinue = {
                if (isOnboarding) {
                    onEvent(LocationAccessEvent.Complete(state.displayedPermissionLevel))
                } else if (flowState == null) {
                    onBack()
                }
            }
        )
    )
}

internal fun LocationAccessCapability.launchLocationAccessEffect(effect: LocationAccessEffect?) {
    when (effect) {
        LocationAccessEffect.RequestForegroundPermission -> requestPermission()
        LocationAccessEffect.OpenForegroundSettings -> openAppSettings()
        LocationAccessEffect.LaunchBackgroundPermission -> launchBackgroundPermission()
        null -> Unit
    }
}
