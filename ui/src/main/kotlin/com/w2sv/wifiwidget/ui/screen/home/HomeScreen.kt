package com.w2sv.wifiwidget.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.composed.ui.platform.isLandscapeModeActive
import com.w2sv.composed.ui.thenIf
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarHost
import com.w2sv.wifiwidget.ui.designsystem.IconHeadline
import com.w2sv.wifiwidget.ui.designsystem.theme.ThemeController
import com.w2sv.wifiwidget.ui.screen.home.components.navdrawer.NavDrawer
import com.w2sv.wifiwidget.ui.screen.home.components.navdrawer.NavDrawerTopBar
import com.w2sv.wifiwidget.ui.screen.home.components.widget.WidgetActionsSection
import com.w2sv.wifiwidget.ui.screen.home.components.wifistatus.WifiInformationList
import com.w2sv.wifiwidget.ui.screen.home.model.wifistate.WifiState
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarBuilderFlow
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    themeController: ThemeController,
    wifiState: WifiState,
    pinWidget: () -> Unit,
    toWidgetConfiguration: () -> Unit,
    snackbarBuilderFlow: SnackbarBuilderFlow,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
) {
    val scope = rememberCoroutineScope()
    NavDrawer(state = drawerState, themeController = themeController) {
        Scaffold(
            topBar = {
                NavDrawerTopBar(
                    title = { Text(stringResource(id = R.string.app_name)) },
                    onNavigationIconClick = { scope.launch { drawerState.open() } }
                )
            },
            snackbarHost = { AppSnackbarHost(snackbarBuilderFlow = snackbarBuilderFlow) },
            contentWindowInsets = WindowInsets()
        ) { paddingValues ->
            val modifier = Modifier.padding(paddingValues)
            if (isLandscapeModeActive) {
                LandscapeHomeContent(wifiState, pinWidget, toWidgetConfiguration, modifier)
            } else {
                PortraitHomeContent(wifiState, pinWidget, toWidgetConfiguration, modifier)
            }
        }
    }
}

@Composable
private fun PortraitHomeContent(
    wifiState: WifiState,
    pinWidget: () -> Unit,
    toWidgetConfiguration: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .padding(horizontal = HomeScreenTokens.screenHorizontalPadding)
            .padding(top = HomeScreenTokens.screenTopPadding)
    ) {
        WidgetActionsSection(pinWidget = pinWidget, toWidgetConfiguration = toWidgetConfiguration)
        Spacer(Modifier.height(22.dp))
        WifiInformationSection(wifiState = wifiState, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LandscapeHomeContent(
    wifiState: WifiState,
    pinWidget: () -> Unit,
    toWidgetConfiguration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasProperties = wifiState.asConnectedOrNull?.propertyPresentations.orEmpty().isNotEmpty()

    Row(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .padding(horizontal = HomeScreenTokens.screenHorizontalPadding)
            .padding(top = HomeScreenTokens.screenTopPadding),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(bottom = HomeScreenTokens.scrollContentBottomPadding),
            contentAlignment = Alignment.Center
        ) {
            WidgetActionsSection(
                pinWidget = pinWidget,
                toWidgetConfiguration = toWidgetConfiguration,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        }
        Box(
            modifier = Modifier
                .weight(3f)
                .fillMaxHeight()
                .padding(bottom = if (hasProperties) 0.dp else HomeScreenTokens.scrollContentBottomPadding),
            contentAlignment = Alignment.CenterStart
        ) {
            WifiInformationSection(
                wifiState = wifiState,
                modifier = Modifier.thenIf(hasProperties, onTrue = { fillMaxSize() }, onFalse = { fillMaxWidth() }),
                fillAvailableHeight = hasProperties
            )
        }
    }
}

@Composable
private fun WifiInformationSection(
    wifiState: WifiState,
    modifier: Modifier = Modifier,
    fillAvailableHeight: Boolean = true
) {
    Column(modifier = modifier) {
        IconHeadline(stringRes = R.string.wifi_status, modifier = Modifier.fillMaxWidth())
        Text(
            text = stringResource(R.string.live_wifi_connection_details),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        WifiInformationList(
            wifiState = wifiState,
            modifier = Modifier.thenIf(fillAvailableHeight) { weight(1f) },
            fillAvailableHeight = fillAvailableHeight
        )
    }
}
