package com.w2sv.wifiwidget.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.composed.core.CollectFromFlow
import com.w2sv.composed.core.isLandscapeModeActive
import com.w2sv.wifiwidget.ui.LocalLocationAccessState
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarHost
import com.w2sv.wifiwidget.ui.designsystem.NavigationDrawerScreenTopAppBar
import com.w2sv.wifiwidget.ui.designsystem.drawer.NavigationDrawer
import com.w2sv.wifiwidget.ui.screens.home.components.LocationAccessRationals
import com.w2sv.wifiwidget.ui.screens.home.components.widget.WidgetCard
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.WifiStatusCard
import com.w2sv.wifiwidget.ui.states.LocationAccessState
import com.w2sv.wifiwidget.ui.utils.ModifierReceivingComposable
import com.w2sv.wifiwidget.ui.utils.rememberMovableContentOf
import kotlinx.coroutines.launch
import slimber.log.i
import java.util.Calendar

@Composable
fun HomeScreen(
    locationAccessState: LocationAccessState = LocalLocationAccessState.current,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    homeScreenVM: HomeScreenViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    NavigationDrawer(state = drawerState) {
        Scaffold(
            topBar = { NavigationDrawerScreenTopAppBar { scope.launch { drawerState.open() } } },
            snackbarHost = { AppSnackbarHost() }
        ) { paddingValues ->
            val wifiState by homeScreenVM.wifiState.collectAsStateWithLifecycle()

            val wifiStatusCard: ModifierReceivingComposable = rememberMovableContentOf {
                WifiStatusCard(wifiState = wifiState, modifier = it)
            }
            val widgetCard: ModifierReceivingComposable = rememberMovableContentOf {
                WidgetCard(locationAccessState = locationAccessState, modifier = it)
            }

            if (isLandscapeModeActive) {
                LandscapeMode(
                    paddingValues = paddingValues,
                    wifiStatusCard = wifiStatusCard,
                    widgetCard = widgetCard
                )
            } else {
                PortraitMode(
                    paddingValues = paddingValues,
                    wifiStatusCard = wifiStatusCard,
                    widgetCard = widgetCard
                )
            }
        }

        CollectFromFlow(locationAccessState.newStatus) {
            i { "Triggering HomeScreenViewModel.onLocationAccessChanged on new location access status $it" }
            homeScreenVM.onLocationAccessChanged()
        }

        LocationAccessRationals(state = locationAccessState)
    }
}

@Composable
private fun LandscapeMode(
    paddingValues: PaddingValues,
    wifiStatusCard: ModifierReceivingComposable,
    widgetCard: ModifierReceivingComposable
) {
    Row(
        modifier = Modifier
            .padding(paddingValues)
            .padding(vertical = 16.dp)
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        wifiStatusCard(Modifier.fillMaxWidth(0.4f))
        widgetCard(Modifier.fillMaxWidth(0.7f))
    }
}

@Composable
private fun PortraitMode(
    paddingValues: PaddingValues,
    wifiStatusCard: ModifierReceivingComposable,
    widgetCard: ModifierReceivingComposable
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(bottom = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.15f))
        wifiStatusCard(Modifier.fillMaxWidth(0.8f))
        Spacer(Modifier.weight(0.15f))
        widgetCard(Modifier.fillMaxWidth(0.84f))
        Spacer(Modifier.weight(0.15f))
        CopyrightText()
    }
}

@Composable
private fun CopyrightText(modifier: Modifier = Modifier) {
    Text(
        text = remember { "© 2022 - ${Calendar.getInstance().get(Calendar.YEAR)} | W2SV" },
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 16.sp,
        modifier = modifier
    )
}
