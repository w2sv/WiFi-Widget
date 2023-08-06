package com.w2sv.wifiwidget.ui.screens.home

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.coroutines.reset
import com.w2sv.data.model.WifiProperty
import com.w2sv.widget.utils.attemptWifiWidgetPin
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppTopBar
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.drawer.NavigationDrawer
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.BackgroundLocationAccessRationalDialog
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LAPRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRationalDialog
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequest
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.WidgetConfigurationDialog
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.InfoDialog
import com.w2sv.wifiwidget.ui.viewmodels.HomeScreenViewModel
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel
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
                        drawerState.open()
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
                                    LAPRequestTrigger.PinWidgetButtonPress

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
                        widgetConfigurationVM.wifiProperties[WifiProperty.SSID] = true
                        widgetConfigurationVM.wifiProperties[WifiProperty.BSSID] = true
                        widgetConfigurationVM.wifiProperties.sync()
                        attemptWifiWidgetPin(context)
                    },
                    onDenied = {
                        attemptWifiWidgetPin(context)
                    }
                )

                is LAPRequestTrigger.PropertyCheckChange -> LocationAccessPermissionRequest(
                    onGranted = {
                        widgetConfigurationVM.wifiProperties[trigger.property] = true
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
                InfoDialog(
                    labelRes = it.viewData.labelRes,
                    descriptionRes = it.viewData.descriptionRes,
                    learnMoreUrl = it.viewData.learnMoreUrl,
                    onDismissRequest = { widgetConfigurationVM.infoDialogProperty.reset() }
                )
            }

            widgetConfigurationVM.refreshPeriodicallyInfoDialog.collectAsState().apply {
                if (value) {
                    InfoDialog(
                        labelRes = R.string.refresh_periodically,
                        descriptionRes = R.string.refresh_periodically_info,
                        onDismissRequest = {
                            widgetConfigurationVM.refreshPeriodicallyInfoDialog.value = false
                        }
                    )
                }
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
fun PinWidgetButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(160.dp, 60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 16.dp)
    ) {
        JostText(
            text = stringResource(R.string.pin_widget),
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