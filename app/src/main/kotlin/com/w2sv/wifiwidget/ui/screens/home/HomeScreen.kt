package com.w2sv.wifiwidget.ui.screens.home

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppSnackbar
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.AppTopBar
import com.w2sv.wifiwidget.ui.components.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.components.drawer.NavigationDrawer
import com.w2sv.wifiwidget.ui.components.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessRationals
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.rememberLocationAccessPermissionState
import com.w2sv.wifiwidget.ui.screens.home.components.widget.WidgetCard
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.WifiStatusCard
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.model.WifiState
import com.w2sv.wifiwidget.ui.utils.CollectLatestFromFlow
import com.w2sv.wifiwidget.ui.utils.isLandscapeModeActivated
import com.w2sv.wifiwidget.ui.viewmodels.AppViewModel
import com.w2sv.wifiwidget.ui.viewmodels.HomeScreenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = viewModel(),
    homeScreenVM: HomeScreenViewModel = viewModel(),
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
        state = drawerState,
        modifier = modifier
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
                CollectLatestFromFlow(appViewModel.snackbarVisualsFlow) {
                    snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(it(context))
                }

                SnackbarHost(snackbarHostState) { snackbarData ->
                    AppSnackbar(visuals = snackbarData.visuals as AppSnackbarVisuals)
                }
            },
        ) { paddingValues ->
            val wifiState by homeScreenVM.wifiState.collectAsStateWithLifecycle()

            if (isLandscapeModeActivated) {
                LandscapeMode(
                    paddingValues = paddingValues,
                    wifiState = wifiState,
                    locationAccessState = locationAccessState
                )
            } else {
                PortraitMode(
                    paddingValues = paddingValues,
                    wifiState = wifiState,
                    locationAccessState = locationAccessState
                )
            }
        }

        LocationAccessRationals(state = locationAccessState)

        BackHandler {
            when (drawerState.isOpen) {
                true -> scope.launch {
                    drawerState.close()
                }

                false -> appViewModel.onBackPress()?.let {
                    context.showToast(it)
                }
            }
        }
    }
}

@Composable
private fun LandscapeMode(
    paddingValues: PaddingValues,
    wifiState: WifiState,
    locationAccessState: LocationAccessState,
) {
    Row(
        modifier = Modifier
            .padding(paddingValues)
            .padding(vertical = 16.dp)
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        WifiStatusCard(
            wifiState = wifiState,
            modifier = Modifier
                .fillMaxWidth(0.4f),
            propertyDisplayModifier = Modifier
                .fillMaxHeight()
        )

        WidgetCard(
            locationAccessState = locationAccessState,
            modifier = Modifier.fillMaxWidth(0.7f),
        )
    }
}

@Composable
private fun PortraitMode(
    paddingValues: PaddingValues,
    wifiState: WifiState,
    locationAccessState: LocationAccessState,
) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.15f))
        WifiStatusCard(
            wifiState = wifiState,
            modifier = Modifier
                .fillMaxWidth(0.77f),
            propertyDisplayModifier = Modifier
                .fillMaxHeight(0.28f)
        )

        Spacer(Modifier.weight(0.2f))

        WidgetCard(
            locationAccessState = locationAccessState,
            modifier = Modifier
                .fillMaxWidth(0.8f),
        )
        Spacer(Modifier.weight(0.3f))
        CopyrightText(modifier = Modifier.padding(bottom = 10.dp))
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
