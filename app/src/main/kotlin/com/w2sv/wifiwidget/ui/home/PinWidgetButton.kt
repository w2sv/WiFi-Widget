package com.w2sv.wifiwidget.ui.home

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.activities.HomeActivity
import com.w2sv.wifiwidget.ui.JostText

@Composable
fun PinWidgetButton(
    modifier: Modifier = Modifier,
    viewModel: HomeActivity.ViewModel = viewModel()
) {
    var triggerOnClickListener by rememberSaveable {
        mutableStateOf(false)
    }

    StatelessPinWidgetButton(modifier) {
        triggerOnClickListener = true
    }

    if (triggerOnClickListener) {
        when (viewModel.lapDialogAnswered) {
            false -> LocationAccessPermissionDialog(trigger = LocationAccessPermissionDialogTrigger.PinWidgetButtonPress) {
                triggerOnClickListener = false
            }
            true -> {
                WifiWidgetProvider.pinWidget(LocalContext.current)
                triggerOnClickListener = false
            }
        }
    }
}

@Composable
private fun StatelessPinWidgetButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
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