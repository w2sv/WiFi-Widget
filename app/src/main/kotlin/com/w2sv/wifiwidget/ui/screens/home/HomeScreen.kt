package com.w2sv.wifiwidget.ui.screens.home

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.coroutines.reset
import com.w2sv.data.model.WifiProperty
import com.w2sv.widget.utils.attemptWifiWidgetPin
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppSnackbar
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.AppTopBar
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.drawer.NavigationDrawer
import com.w2sv.wifiwidget.ui.components.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.BackgroundLocationAccessRationalDialog
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LAPRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRationalDialog
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequest
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.WidgetConfigurationDialog
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.InfoDialog
import com.w2sv.wifiwidget.ui.screens.home.components.wifi_connection_info.WifiConnectionInfoCard
import com.w2sv.wifiwidget.ui.viewmodels.HomeScreenViewModel
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
internal fun HomeScreen(
    homeScreenVM: HomeScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

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
                SnackbarHost(homeScreenVM.snackbarHostState) { snackbarData ->
                    AppSnackbar(visuals = snackbarData.visuals as AppSnackbarVisuals)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(0.8f)
                        .fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.weight(0.15f))
                        Box(
                            modifier = Modifier
                                .weight(0.8f)
                                .fillMaxWidth(0.77f),
                            contentAlignment = Alignment.Center
                        ) {
                            WifiConnectionInfoCard(
                                wifiStatus = homeScreenVM.wifiStatus.collectAsState().value,
                                wifiPropertiesViewData = homeScreenVM.wifiPropertiesViewData.collectAsState().value,
                                showSnackbar = {
                                    scope.launch {
                                        homeScreenVM.snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                                            it
                                        )
                                    }
                                }
                            )
                        }
                        Spacer(Modifier.weight(0.2f))

                        ElevatedCard(elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 24.dp, vertical = 32.dp)
                                    .fillMaxWidth(0.85f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CardHeader(iconRes = R.drawable.ic_widgets_24, header = "Widget")
                                Spacer(modifier = Modifier.height(32.dp))
                                WidgetInteractionElementsRow(
                                    onPinWidgetButtonClick = {
                                        when (homeScreenVM.lapRationalShown) {
                                            false -> homeScreenVM.lapRationalTrigger.value =
                                                LAPRequestTrigger.PinWidgetButtonPress

                                            true -> attemptWifiWidgetPin(context)
                                        }
                                    },
                                    onWidgetConfigurationButtonClick = {
                                        homeScreenVM.showWidgetConfigurationDialog.value = true
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.weight(0.3f))
                    }
                }

                CopyrightText(modifier = Modifier.padding(bottom = 10.dp))
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
fun CardHeader(@DrawableRes iconRes: Int, header: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Box(modifier = Modifier.weight(0.3f)) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
        }
        Box(Modifier.weight(0.7f), contentAlignment = Alignment.Center) {
            JostText(
                text = header,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.weight(0.3f))
    }
}

@Composable
private fun WidgetInteractionElementsRow(
    onPinWidgetButtonClick: () -> Unit,
    onWidgetConfigurationButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        PinWidgetButton(
            onClick = onPinWidgetButtonClick,
            modifier = Modifier.size(180.dp, 60.dp)
        )

        Spacer(modifier = Modifier.width(32.dp))

        WidgetConfigurationDialogButton(
            onClick = onWidgetConfigurationButtonClick,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun OverlayDialogs(
    homeScreenVM: HomeScreenViewModel = viewModel(),
    widgetVM: WidgetViewModel = viewModel()
) {
    val context = LocalContext.current

    homeScreenVM.lapRationalTrigger.collectAsState().value?.let { trigger ->
        LocationAccessPermissionRationalDialog(
            onProceed = {
                homeScreenVM.onLocationAccessPermissionRationalShown(trigger)
            }
        )
    }
    homeScreenVM.lapRequestTrigger.collectAsState().value?.let { trigger ->
        when (trigger) {
            is LAPRequestTrigger.PinWidgetButtonPress -> LocationAccessPermissionRequest(
                onGranted = {
                    widgetVM.wifiProperties[WifiProperty.SSID] = true
                    widgetVM.wifiProperties[WifiProperty.BSSID] = true
                    widgetVM.wifiProperties.sync()
                    attemptWifiWidgetPin(context)
                },
                onDenied = {
                    attemptWifiWidgetPin(context)
                }
            )

            is LAPRequestTrigger.PropertyCheckChange -> LocationAccessPermissionRequest(
                onGranted = {
                    widgetVM.wifiProperties[trigger.property] = true
                },
                onDenied = {}
            )
        }
    }
    if (homeScreenVM.showWidgetConfigurationDialog.collectAsState().value) {
        WidgetConfigurationDialog(
            closeDialog = {
                homeScreenVM.showWidgetConfigurationDialog.value = false
            }
        )

        widgetVM.infoDialogData.collectAsState().value?.let {
            InfoDialog(
                data = it,
                onDismissRequest = { widgetVM.infoDialogData.reset() }
            )
        }
    }
    @SuppressLint("NewApi")
    if (homeScreenVM.showBackgroundLocationAccessRational.collectAsState().value) {
        BackgroundLocationAccessRationalDialog(
            onDismissRequest = {
                homeScreenVM.showBackgroundLocationAccessRational.value = false
            }
        )
    }
}

@Composable
fun PinWidgetButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 16.dp)
    ) {
        JostText(
            text = stringResource(R.string.pin),
            fontSize = 16.sp
        )
    }
}

@Composable
fun WidgetConfigurationDialogButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = stringResource(R.string.inflate_the_widget_configuration_dialog),
            modifier = modifier,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun CopyrightText(modifier: Modifier = Modifier) {
    JostText(
        text = "© 2022 - ${Calendar.getInstance().get(Calendar.YEAR)} | W2SV",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 16.sp,
        modifier = modifier
    )
}