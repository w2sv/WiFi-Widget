package com.w2sv.wifiwidget.ui.screens.home

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.widget.WidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.shared.JostText

@Composable
fun StatefulPinWidgetButton(
    modifier: Modifier = Modifier,
    homeScreenViewModel: HomeScreenViewModel = viewModel()
) {
    val context = LocalContext.current

    PinWidgetButton(modifier) {
        when (homeScreenViewModel.lapRationalShown) {
            false -> homeScreenViewModel.lapRationalTrigger.value =
                LAPRequestTrigger.PinWidgetButtonPress

            true -> WidgetProvider.pinWidget(context)
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