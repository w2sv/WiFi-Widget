package com.w2sv.wifiwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            MainScreen()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun MainScreen() {
    ScaffoldWithTopAppBar {
        Column(
            Modifier
                .padding(it)
                .fillMaxHeight()
                .fillMaxWidth(),
            Arrangement.Center,
            Alignment.CenterHorizontally
        ) {
            PinAppWidgetButton()
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
@Preview
@Composable
fun PinAppWidgetButton() {
    val context = LocalContext.current

    ElevatedButton(
        {
            with(context.getSystemService(AppWidgetManager::class.java)) {
                if (isRequestPinAppWidgetSupported) {
                    requestPinAppWidget(
                        ComponentName(
                            context,
                            WidgetProvider::class.java
                        ),
                        null,
                        null
                    )
                }
            }
        },
        modifier = Modifier.defaultMinSize(140.dp, 60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue_chill_dark)),
        content = {
            Text(
                stringResource(R.string.pin_widget),
                color = Color.White
            )
        }
    )
}