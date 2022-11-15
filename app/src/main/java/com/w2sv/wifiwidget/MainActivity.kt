package com.w2sv.wifiwidget

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.w2sv.typedpreferences.extensions.appPreferences
import com.w2sv.wifiwidget.preferences.BooleanPreferences

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            MainScreen(pinAppWidgetButton = {
                PinAppWidgetButton {
                    if (!BooleanPreferences.locationPermissionDialogShown)
                        LocationPermissionDialog(
                            {
                                locationPermissionRequestLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                                onLocationPermissionDialogClosed(it)
                            },
                            {
                                BooleanPreferences.showSSID = false
                                requestPinWidget()
                                onLocationPermissionDialogClosed(it)
                            }
                        )
                    else
                        requestPinWidget()
                }
            })
        }
    }

    private fun onLocationPermissionDialogClosed(onClosed: () -> Unit) {
//        appPreferences().locationPermissionDialogShown = true
        onClosed()
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private val locationPermissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            requestPinWidget()
        }

    override fun onDestroy() {
        super.onDestroy()

        BooleanPreferences.writeChangedValues(appPreferences())
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview
fun MainScreenPreview() {
    MainScreen {
        PinAppWidgetButton {}
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(pinAppWidgetButton: @Composable () -> Unit) {
    BottomSheetLayout {
        ScaffoldWithTopAppBar {
            Column(
                Modifier
                    .padding(it)
                    .fillMaxHeight()
                    .fillMaxWidth(),
                Arrangement.Center,
                Alignment.CenterHorizontally
            ) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Filled.Settings, stringResource(id = R.string.configure_widget))
                }
                pinAppWidgetButton()
            }
        }
    }
}