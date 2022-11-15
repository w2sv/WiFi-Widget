package com.w2sv.wifiwidget

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

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
        contentColor = Color.White
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