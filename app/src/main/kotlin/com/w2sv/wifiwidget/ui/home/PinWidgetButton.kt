package com.w2sv.wifiwidget.ui.home

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.activities.HomeActivity
import com.w2sv.wifiwidget.ui.home.model.LocationAccessPermissionDialogTrigger
import com.w2sv.wifiwidget.ui.shared.JostText

@Composable
fun StatefulPinWidgetButton(
    modifier: Modifier = Modifier,
    viewModel: HomeActivity.ViewModel = viewModel()
) {
    val context = LocalContext.current

    PinWidgetButton(modifier) {
        when (viewModel.lapDialogAnswered) {
            false -> viewModel.lapDialogTrigger.value =
                LocationAccessPermissionDialogTrigger.PinWidgetButtonPress

            true -> WifiWidgetProvider.pinWidget(context)
        }
    }

    viewModel.lapDialogTrigger.collectAsState().apply {
        if (value == LocationAccessPermissionDialogTrigger.PinWidgetButtonPress) {
            LocationAccessPermissionDialog(LocationAccessPermissionDialogTrigger.PinWidgetButtonPress)
        }
    }
}

@Composable
private fun PinWidgetButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ElevatedButton(
        onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        content = {
            JostText(
                text = stringResource(R.string.pin_widget)
            )
        }
    )
}