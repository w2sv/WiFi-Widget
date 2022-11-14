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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
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
fun MainScreenPreview(){
    MainScreen {
        PinAppWidgetButton {
            LocationPermissionDialog(onConfirm = { /*TODO*/ }) {
                
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(pinAppWidgetButton: @Composable () -> Unit) {
    ScaffoldWithTopAppBar {
        Column(
            Modifier
                .padding(it)
                .fillMaxHeight()
                .fillMaxWidth(),
            Arrangement.Center,
            Alignment.CenterHorizontally
        ) {
            pinAppWidgetButton()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldWithTopAppBar(content: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                { Text(stringResource(id = R.string.app_name)) },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = colorResource(
                        id = R.color.blue_chill_dark
                    ),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) {
        content(it)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LocationPermissionDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        confirmButton = {
            ElevatedButton(onConfirm) { Text(text = "Go ahead") }
        },
        dismissButton = {
            ElevatedButton(onDismiss) {
                Text(text = "Ignore SSID")
            }
        },
        onDismissRequest = {},
        text = { Text(text = "If you want your SSID to be displayed, you'll have to grant location access") },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PinAppWidgetButton(onClickListener: @Composable (() -> Unit) -> Unit) {
    val openDialog = remember {
        mutableStateOf(false)
    }

    ElevatedButton(
        { openDialog.value = true },
        modifier = Modifier.defaultMinSize(140.dp, 60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue_chill_dark)),
        content = {
            Text(
                stringResource(R.string.pin_widget),
                color = Color.White
            )
        }
    )

    if (openDialog.value) {
        onClickListener { openDialog.value = false }
    }
}