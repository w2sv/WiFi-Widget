package com.w2sv.wifiwidget.ui.screens.home

import android.annotation.SuppressLint
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
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.coroutines.reset
import com.w2sv.data.model.WifiProperty
import com.w2sv.widget.utils.attemptWifiWidgetPin
import com.w2sv.wifiwidget.ui.components.AppSnackbar
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.AppTopBar
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.drawer.NavigationDrawer
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.BackgroundLocationAccessRationalDialog
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRationalDialog
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequest
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequiringAction
import com.w2sv.wifiwidget.ui.screens.home.components.widget.WidgetCard
import com.w2sv.wifiwidget.ui.screens.home.components.widget.WidgetInteractionElementsRow
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.WidgetConfigurationDialog
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.PropertyInfoDialog
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.WifiConnectionInfoCard
import com.w2sv.wifiwidget.ui.utils.landscapeModeActivated
import com.w2sv.wifiwidget.ui.viewmodels.HomeScreenViewModel
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
internal fun HomeScreen(
    homeScreenVM: HomeScreenViewModel = viewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    NavigationDrawer(
        state = drawerState,
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
                SnackbarHost(homeScreenVM.snackbarHostState) { snackbarData ->
                    AppSnackbar(visuals = snackbarData.visuals as AppSnackbarVisuals)
                }
            },
        ) { paddingValues ->
            if (landscapeModeActivated) {
                LandscapeMode(paddingValues = paddingValues)
            } else {
                PortraitMode(paddingValues = paddingValues)
            }
        }

        OverlayDialogs()

        BackHandler {
            when (drawerState.isOpen) {
                true -> scope.launch {
                    drawerState.close()
                }

                false -> homeScreenVM.onBackPress(context)
            }
        }
    }
}

@Composable
fun LandscapeMode(paddingValues: PaddingValues, homeScreenVM: HomeScreenViewModel = viewModel()) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .padding(paddingValues)
            .padding(vertical = 16.dp)
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        WifiConnectionInfoCard(
            wifiStatus = homeScreenVM.wifiStatusUIState.status.collectAsState().value,
            wifiPropertiesViewData = homeScreenVM.wifiStatusUIState.propertiesViewData.collectAsState().value,
            showSnackbar = homeScreenVM::showSnackbar,
            modifier = Modifier.fillMaxWidth(0.4f),
        )

        WidgetCard(
            widgetInteractionElementsRow = {
                WidgetInteractionElementsRow(
                    onPinWidgetButtonClick = {
                        when (homeScreenVM.lapUIState.rationalShown) {
                            false ->
                                homeScreenVM.lapUIState.rationalTriggeringAction.value =
                                    LocationAccessPermissionRequiringAction.PinWidgetButtonPress

                            true -> attemptWifiWidgetPin(context)
                        }
                    },
                    onWidgetConfigurationButtonClick = {
                        homeScreenVM.showWidgetConfigurationDialog.value = true
                    },
                )
            },
            modifier = Modifier.fillMaxWidth(0.6f),
        )
    }
}

@Composable
private fun PortraitMode(
    paddingValues: PaddingValues,
    homeScreenVM: HomeScreenViewModel = viewModel(),
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.15f))
        WifiConnectionInfoCard(
            wifiStatus = homeScreenVM.wifiStatusUIState.status.collectAsState().value,
            wifiPropertiesViewData = homeScreenVM.wifiStatusUIState.propertiesViewData.collectAsState().value,
            showSnackbar = homeScreenVM::showSnackbar,
            modifier = Modifier
                .weight(0.8f)
                .fillMaxWidth(0.77f),
        )

        Spacer(Modifier.weight(0.2f))

        WidgetCard(
            widgetInteractionElementsRow = {
                WidgetInteractionElementsRow(
                    onPinWidgetButtonClick = {
                        when (homeScreenVM.lapUIState.rationalShown) {
                            false ->
                                homeScreenVM.lapUIState.rationalTriggeringAction.value =
                                    LocationAccessPermissionRequiringAction.PinWidgetButtonPress

                            true -> attemptWifiWidgetPin(context)
                        }
                    },
                    onWidgetConfigurationButtonClick = {
                        homeScreenVM.showWidgetConfigurationDialog.value = true
                    },
                )
            },
            modifier = Modifier.fillMaxWidth(0.8f),
        )
        Spacer(Modifier.weight(0.3f))
        CopyrightText(modifier = Modifier.padding(bottom = 10.dp))
    }
}

@Composable
private fun OverlayDialogs(
    homeScreenVM: HomeScreenViewModel = viewModel(),
    widgetVM: WidgetViewModel = viewModel(),
) {
    val context = LocalContext.current

    homeScreenVM.lapUIState.rationalTriggeringAction.collectAsState().value?.let {
        LocationAccessPermissionRationalDialog(
            onProceed = {
                homeScreenVM.lapUIState.onRationalShown()
            },
        )
    }
    homeScreenVM.lapUIState.requestLaunchingAction.collectAsState().value?.let { trigger ->
        when (trigger) {
            is LocationAccessPermissionRequiringAction.PinWidgetButtonPress -> LocationAccessPermissionRequest(
                lapUIState = homeScreenVM.lapUIState,
                onGranted = {
                    widgetVM.configuration.wifiProperties[WifiProperty.SSID] = true
                    widgetVM.configuration.wifiProperties[WifiProperty.BSSID] = true
                    widgetVM.configuration.wifiProperties.sync()
                    attemptWifiWidgetPin(context)
                },
                onDenied = {
                    attemptWifiWidgetPin(context)
                },
            )

            is LocationAccessPermissionRequiringAction.PropertyCheckChange -> LocationAccessPermissionRequest(
                lapUIState = homeScreenVM.lapUIState,
                onGranted = {
                    widgetVM.configuration.wifiProperties[trigger.property] = true
                },
                onDenied = {},
            )
        }
    }
    if (homeScreenVM.showWidgetConfigurationDialog.collectAsState().value) {
        WidgetConfigurationDialog(
            closeDialog = {
                homeScreenVM.showWidgetConfigurationDialog.value = false
            },
        )

        widgetVM.propertyInfoDialogData.collectAsState().value?.let {
            PropertyInfoDialog(
                data = it,
                onDismissRequest = { widgetVM.propertyInfoDialogData.reset() },
            )
        }
    }
    @SuppressLint("NewApi")
    if (homeScreenVM.lapUIState.showBackgroundAccessRational.collectAsState().value) {
        BackgroundLocationAccessRationalDialog(
            onDismissRequest = {
                homeScreenVM.lapUIState.showBackgroundAccessRational.value = false
            },
        )
    }
}

@Composable
private fun CopyrightText(modifier: Modifier = Modifier) {
    JostText(
        text = "© 2022 - ${Calendar.getInstance().get(Calendar.YEAR)} | W2SV",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 16.sp,
        modifier = modifier,
    )
}
