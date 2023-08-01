package com.w2sv.wifiwidget.ui.screens.home

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.coroutines.reset
import com.w2sv.data.model.WifiProperty
import com.w2sv.widget.utils.attemptWifiWidgetPin
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppTopBar
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.navigationdrawer.NavigationDrawer
import com.w2sv.wifiwidget.ui.components.navigationdrawer.closeDrawer
import com.w2sv.wifiwidget.ui.components.navigationdrawer.openDrawer
import com.w2sv.wifiwidget.ui.screens.home.components.PinWidgetButton
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.BackgroundLocationAccessRational
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRational
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequest
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.WidgetConfigurationDialog
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.WidgetConfigurationDialogButton
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.WidgetViewModel
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.PropertyInfoDialog
import kotlinx.coroutines.launch
import java.util.*

@Composable
internal fun HomeScreen(
    homeScreenVM: HomeScreenViewModel = viewModel(),
    widgetConfigurationVM: WidgetViewModel = viewModel()
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
                        drawerState.openDrawer()
                    }
                }
            }
        ) { paddingValues ->
            Column(
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                Arrangement.SpaceBetween,
                Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(1.5f))
                Box(Modifier.weight(0.5f)) {
                    PinWidgetButton(
                        onClick = {
                            when (homeScreenVM.lapRationalShown) {
                                false -> homeScreenVM.lapRationalTrigger.value =
                                    LocationAccessPermissionRequestTrigger.PinWidgetButtonPress

                                true -> attemptWifiWidgetPin(context)
                            }
                        }
                    )
                }

                Spacer(Modifier.weight(0.5f))
                Box(Modifier.weight(1f)) {
                    WidgetConfigurationDialogButton(
                        onClick = {
                            homeScreenVM.showWidgetConfigurationDialog.value = true
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }

                CopyrightText(modifier = Modifier.padding(bottom = dimensionResource(R.dimen.margin_minimal)))
            }
        }
        homeScreenVM.lapRationalTrigger.collectAsState().value?.let { trigger ->
            LocationAccessPermissionRational(
                onProceed = {
                    homeScreenVM.onLocationAccessPermissionRationalShown(trigger)
                }
            )
        }
        homeScreenVM.lapRequestTrigger.collectAsState().value?.let { trigger ->
            when (trigger) {
                LocationAccessPermissionRequestTrigger.PinWidgetButtonPress -> LocationAccessPermissionRequest(
                    onGranted = {
                        widgetConfigurationVM.setWifiProperties[WifiProperty.SSID] = true
                        widgetConfigurationVM.setWifiProperties.sync()
                        attemptWifiWidgetPin(context)
                    },
                    onDenied = {
                        attemptWifiWidgetPin(context)
                    }
                )

                LocationAccessPermissionRequestTrigger.SSIDCheck -> LocationAccessPermissionRequest(
                    onGranted = {
                        widgetConfigurationVM.setWifiProperties[WifiProperty.SSID] = true
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

            widgetConfigurationVM.infoDialogProperty.collectAsState().value?.let {
                PropertyInfoDialog(
                    property = it,
                    onDismissRequest = { widgetConfigurationVM.infoDialogProperty.reset() }
                )
            }
        }
        @SuppressLint("NewApi")
        if (homeScreenVM.showBackgroundLocationAccessRational.collectAsState().value) {
            BackgroundLocationAccessRational(
                onDismissRequest = {
                    homeScreenVM.showBackgroundLocationAccessRational.value = false
                }
            )
        }
        BackHandler {
            when (drawerState.isOpen) {
                true -> scope.launch {
                    drawerState.closeDrawer()
                }

                false -> homeScreenVM.onBackPress(context)
            }
        }
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