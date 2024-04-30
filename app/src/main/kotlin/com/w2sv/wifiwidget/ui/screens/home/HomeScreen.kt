package com.w2sv.wifiwidget.ui.screens.home

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.w2sv.composed.isLandscapeModeActive
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbar
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.AppTopBar
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.drawer.NavigationDrawer
import com.w2sv.wifiwidget.ui.designsystem.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessRationals
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.rememberLocationAccessPermissionState
import com.w2sv.wifiwidget.ui.screens.home.components.widget.WidgetCard
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.WifiStatusCard
import com.w2sv.wifiwidget.ui.utils.CollectLatestFromFlow
import com.w2sv.wifiwidget.ui.viewmodels.AppViewModel
import com.w2sv.wifiwidget.ui.viewmodels.HomeScreenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar

private typealias ModifierReceivingComposable = @Composable (Modifier) -> Unit

@Destination<RootGraph>//(start = true)
@Composable
fun HomeScreen(
    appVM: AppViewModel = hiltViewModel(),
    homeScreenVM: HomeScreenViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
    scope: CoroutineScope = rememberCoroutineScope(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    locationAccessState: LocationAccessState = rememberLocationAccessPermissionState(
        permissionRepository = homeScreenVM.permissionRepository,
        saveLocationAccessPermissionRequestLaunched = homeScreenVM::saveLocationAccessPermissionRequestLaunched,
        saveLocationAccessRationalShown = homeScreenVM::saveLocationAccessRationalShown,
        scope = scope
    )
) {
    NavigationDrawer(
        state = drawerState
    ) {
        Scaffold(
            topBar = {
                AppTopBar {
                    scope.launch {
                        drawerState.open()
                    }
                }
            },
            snackbarHost = {
                val snackbarHostState = LocalSnackbarHostState.current

                // Show Snackbars collected from sharedSnackbarVisuals
                CollectLatestFromFlow(appVM.snackbarVisualsFlow) {
                    snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(it(context))
                }

                SnackbarHost(snackbarHostState) { snackbarData ->
                    AppSnackbar(visuals = snackbarData.visuals as AppSnackbarVisuals)
                }
            },
        ) { paddingValues ->
            val wifiState by homeScreenVM.wifiState.collectAsStateWithLifecycle()

            val wifiStatusCard: ModifierReceivingComposable = remember {
                movableContentOf { mod ->
                    WifiStatusCard(wifiState = wifiState, modifier = mod)
                }
            }
            val widgetCard: ModifierReceivingComposable = remember {
                movableContentOf { mod ->
                    WidgetCard(locationAccessState = locationAccessState, modifier = mod)
                }
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

        LocationAccessRationals(state = locationAccessState)

        BackHandler(enabled = drawerState.isOpen) {
            scope.launch {
                drawerState.close()
            }
        }
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
        horizontalArrangement = Arrangement.SpaceEvenly,
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
        horizontalAlignment = Alignment.CenterHorizontally,
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
        text = stringResource(
            R.string.copyright_text,
            Calendar.getInstance().get(Calendar.YEAR)
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 16.sp,
        modifier = modifier,
    )
}
