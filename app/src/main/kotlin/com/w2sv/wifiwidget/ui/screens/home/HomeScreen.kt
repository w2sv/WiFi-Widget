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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.BackgroundLocationAccessPermissionHandler
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionHandler
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.backgroundLocationAccessGrantRequired
import com.w2sv.wifiwidget.ui.screens.home.components.widget.WidgetCard
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.WifiStatusCard
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.model.WifiState
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
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

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
                SnackbarHost(snackbarHostState) { snackbarData ->
                    AppSnackbar(visuals = snackbarData.visuals as AppSnackbarVisuals)
                }
            },
        ) { paddingValues ->
            val wifiState by homeScreenVM.wifiState.collectAsStateWithLifecycle()

            if (isLandscapeModeActivated) {
                LandscapeMode(paddingValues = paddingValues, wifiState = wifiState)
            } else {
                PortraitMode(paddingValues = paddingValues, wifiState = wifiState)
            }
        }

        LocationAccessPermissionHandler(state = homeScreenVM.lapState)
        homeScreenVM.lapState.backgroundAccessState?.let {
            if (backgroundLocationAccessGrantRequired) {
                BackgroundLocationAccessPermissionHandler(state = it)
            }
        }

        LaunchedEffect(snackbarHostState) {
            appViewModel.sharedSnackbarVisuals.collect {
                snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(it)
            }
        }

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
        )

        WidgetCard(
            modifier = Modifier.fillMaxWidth(0.6f),
        )
    }
}

@Composable
private fun PortraitMode(
    paddingValues: PaddingValues,
    wifiState: WifiState,
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
        )

        Spacer(Modifier.weight(0.2f))

        WidgetCard(
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
