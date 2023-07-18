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
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppTopBar
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.NavigationDrawer
import com.w2sv.wifiwidget.ui.screens.home.components.PinWidgetButton
import com.w2sv.wifiwidget.ui.screens.home.locationaccesspermission.BackgroundLocationAccessRational
import com.w2sv.wifiwidget.ui.screens.home.locationaccesspermission.LocationAccessPermissionRational
import com.w2sv.wifiwidget.ui.screens.home.locationaccesspermission.LocationAccessPermissionRequest
import com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.StatefulWidgetConfigurationDialogButton
import java.util.*

@Composable
internal fun HomeScreen(
    viewModel: HomeScreenViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current

    NavigationDrawer { openDrawer, closeDrawer, drawerOpen ->
        Scaffold(topBar = { AppTopBar { openDrawer() } }) { paddingValues ->
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
                        Modifier.defaultMinSize(140.dp, 60.dp)
                    )
                }

                Spacer(Modifier.weight(0.5f))
                Box(Modifier.weight(1f)) {
                    StatefulWidgetConfigurationDialogButton(
                        Modifier.size(32.dp)
                    )
                }

                CopyrightText(modifier = Modifier.padding(bottom = dimensionResource(R.dimen.margin_minimal)))
            }
        }
        viewModel.lapRationalTrigger.collectAsState().value?.let {
            LocationAccessPermissionRational(it)
        }
        viewModel.lapRequestTrigger.collectAsState().value?.let {
            LocationAccessPermissionRequest(it)
        }
        @SuppressLint("NewApi")
        if (viewModel.showBackgroundLocationAccessRational.collectAsState().value) {
            BackgroundLocationAccessRational()
        }
        BackHandler {
            when {
                drawerOpen() -> closeDrawer()
                else -> viewModel.onBackPress(context)
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