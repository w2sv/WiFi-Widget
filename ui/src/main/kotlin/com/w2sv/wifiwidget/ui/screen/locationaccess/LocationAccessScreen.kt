package com.w2sv.wifiwidget.ui.screen.locationaccess

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.composed.ui.platform.isLandscapeModeActive
import com.w2sv.composed.ui.thenIf
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.designsystem.animation.EntranceState
import com.w2sv.wifiwidget.ui.designsystem.animation.rememberEntranceState
import com.w2sv.wifiwidget.ui.screen.locationaccess.components.LocationAccessActionPanel
import com.w2sv.wifiwidget.ui.screen.locationaccess.components.LocationAccessActionPanelPresentation
import com.w2sv.wifiwidget.ui.screen.locationaccess.components.LocationAccessContent
import com.w2sv.wifiwidget.ui.screen.locationaccess.state.LocationAccessScreenActions
import com.w2sv.wifiwidget.ui.screen.locationaccess.state.LocationAccessScreenState

@Composable
internal fun LocationAccessScreen(
    state: LocationAccessScreenState,
    actions: LocationAccessScreenActions,
    modifier: Modifier = Modifier
) {
    val entrance = rememberEntranceState(animate = !LocalInspectionMode.current)
    val isLandscape = isLandscapeModeActive
    val panelPresentation = LocationAccessActionPanelPresentation.from(state)

    Scaffold(
        modifier = modifier,
        topBar = {
            if (!state.isOnboarding) {
                TopAppBar(
                    title = { Text(stringResource(R.string.location_access)) },
                    navigationIcon = {
                        IconButton(onClick = actions.onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.navigate_back))
                        }
                    },
                    expandedHeight = 52.dp
                )
            }
        },
        bottomBar = {
            if (!isLandscape) {
                LocationAccessActionPanel(
                    presentation = panelPresentation,
                    actions = actions,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        contentWindowInsets = if (isLandscape) {
            WindowInsets.safeDrawing
        } else {
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
        }
    ) { scaffoldPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding)
            .consumeWindowInsets(scaffoldPadding)

        if (isLandscape) {
            LocationAccessLandscapeLayout(
                onboarding = state.isOnboarding,
                entrance = entrance,
                presentation = panelPresentation,
                actions = actions,
                modifier = contentModifier
            )
        } else {
            LocationAccessPortraitLayout(
                onboarding = state.isOnboarding,
                entrance = entrance,
                modifier = contentModifier
            )
        }
    }
}

@Composable
private fun LocationAccessLandscapeLayout(
    onboarding: Boolean,
    entrance: EntranceState,
    presentation: LocationAccessActionPanelPresentation,
    actions: LocationAccessScreenActions,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(LocationAccessScreenTokens.screenContentPadding),
        horizontalArrangement = Arrangement.spacedBy(LocationAccessScreenTokens.landscapeColumnSpacing)
    ) {
        LocationAccessContent(
            onboarding = onboarding,
            entrance = entrance,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            compact = true
        )
        LocationAccessActionPanel(
            presentation = presentation,
            actions = actions,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            standalone = true
        )
    }
}

@Composable
private fun LocationAccessPortraitLayout(
    onboarding: Boolean,
    entrance: EntranceState,
    modifier: Modifier = Modifier
) {
    LocationAccessContent(
        onboarding = onboarding,
        entrance = entrance,
        modifier = modifier
            .verticalScroll(rememberScrollState())
            // Keep the initial top clearance scrollable during onboarding.
            .thenIf(onboarding) {
                padding(top = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding())
            }
            .padding(LocationAccessScreenTokens.screenContentPadding)
    )
}
