package com.w2sv.wifiwidget.activities.main

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.w2sv.wifiwidget.WiFiWidgetProvider
import com.w2sv.wifiwidget.extensions.disable
import com.w2sv.wifiwidget.preferences.BooleanPreferences
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            MainScreen(::requestPinWidget) {
                locationPermissionRequestLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }
    }

    private fun requestPinWidget() {
        with(getSystemService(AppWidgetManager::class.java)) {
            if (isRequestPinAppWidgetSupported) {
                requestPinAppWidget(
                    ComponentName(
                        this@MainActivity,
                        WiFiWidgetProvider::class.java
                    ),
                    null,
                    null
                )
            }
        }
    }

    private val locationPermissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            requestPinWidget()
        }
}

@Composable
@Preview
fun MainScreenPreview() {
    MainScreen({}, {})
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(requestPinWidget: () -> Unit, launchLocationPermissionRequest: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Expanded,
    )

    val coroutineScope = rememberCoroutineScope()

    BackHandler(sheetState.isVisible) {
        coroutineScope.launch { sheetState.hide() }
    }

    BottomSheetLayout(sheetState) {
        ScaffoldWithTopAppBar {
            Column(
                Modifier
                    .padding(it)
                    .fillMaxHeight()
                    .fillMaxWidth(),
                Arrangement.Center,
                Alignment.CenterHorizontally
            ) {
                val triggerPinAppWidgetButtonOnClickListener = remember {
                    mutableStateOf(false)
                }

                PinAppWidgetButton(triggerPinAppWidgetButtonOnClickListener) {
                    if (!BooleanPreferences.locationPermissionDialogAnswered)
                        LocationPermissionDialog(
                            onConfirm = {
                                launchLocationPermissionRequest()
                            },
                            onDismiss = {
                                BooleanPreferences.showSSID = false
                                requestPinWidget()
                            },
                            onButtonPress = {
                                BooleanPreferences.locationPermissionDialogAnswered = true
                            },
                            onDismissRequest = {
                                triggerPinAppWidgetButtonOnClickListener.disable()
                            }
                        )
                    else
                        requestPinWidget()
                }
            }
        }
    }
}