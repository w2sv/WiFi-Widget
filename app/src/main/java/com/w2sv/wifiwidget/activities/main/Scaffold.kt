package com.w2sv.wifiwidget.activities.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.wifiwidget.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetScaffold(
    content: @Composable (PaddingValues) -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            BottomSheetValue.Collapsed
        )
    )

    BottomSheetScaffold(
        topBar = { TopBar() },
        scaffoldState = scaffoldState,
        sheetContent = { BottomSheet(sheetState = scaffoldState.bottomSheetState) },
        sheetElevation = 0.dp,
        sheetBackgroundColor = Color.Transparent,
        sheetPeekHeight = 48.dp,
        contentColor = Color.White
    ) {
        content(it)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        { Text(stringResource(id = R.string.app_name)) },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = colorResource(
                id = R.color.blue_chill_dark
            ),
            titleContentColor = Color.White
        )
    )
}

@Composable
fun LocationPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onButtonPress: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        confirmButton = {
            ElevatedButton({
                onConfirm()
                onButtonPress()
                onDismiss()
            }) { Text(text = "Go ahead") }
        },
        dismissButton = {
            ElevatedButton({
                onDismiss()
                onButtonPress()
                onDismiss()
            }) {
                Text(text = "Ignore SSID")
            }
        },
        onDismissRequest = onDismissRequest,
        text = { Text(text = "If you want your SSID to be displayed, you'll have to grant location access") }
    )
}

@Composable
fun PinAppWidgetButton(
    triggerOnClickListener: MutableState<Boolean>,
    onClickListener: @Composable () -> Unit
) {
    ElevatedButton(
        { triggerOnClickListener.value = true },
        modifier = Modifier.defaultMinSize(140.dp, 60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue_chill_dark)),
        content = {
            Text(
                stringResource(R.string.pin_widget),
                color = Color.White
            )
        }
    )

    if (triggerOnClickListener.value)
        onClickListener()
}