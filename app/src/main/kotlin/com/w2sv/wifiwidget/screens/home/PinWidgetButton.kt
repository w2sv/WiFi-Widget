package com.w2sv.wifiwidget.screens.home

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.extensions.requireCastActivity
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.JostText

@Preview
@Composable
internal fun PinWidgetButton() {
    val homeActivity = LocalContext.current.requireCastActivity<HomeActivity>()

    var triggerOnClickListener by rememberSaveable {
        mutableStateOf(false)
    }
    var showLocationAccessServiceInformationDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (triggerOnClickListener) {
        OnClickListener(
            { triggerOnClickListener = false },
            { showLocationAccessServiceInformationDialog = true }
        )
    }
    if (showLocationAccessServiceInformationDialog) {
        LocationAccessServiceInformationDialog {
            homeActivity.lapRequestLauncher.launch()
            showLocationAccessServiceInformationDialog = false
        }
    }


    ElevatedButton(
        { triggerOnClickListener = true },
        modifier = Modifier.defaultMinSize(140.dp, 60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue_chill_dark)),
        content = {
            JostText(
                text = stringResource(R.string.pin_widget)
            )
        }
    )
}

@Composable
private fun OnClickListener(
    resetTrigger: () -> Unit,
    showLocationAccessServiceInformationDialog: () -> Unit
) {
    val homeActivity = LocalContext.current.requireCastActivity<HomeActivity>()
    val viewModel: HomeActivity.ViewModel = viewModel()

    if (!viewModel.lapDialogAnswered)
        LocationAccessPermissionDialog(
            onConfirmButtonPressed = {
                showLocationAccessServiceInformationDialog()
            },
            onDismissButtonPressed = {
                homeActivity.requestWidgetPin()
            },
            onDialogAnswered = {
                viewModel.onLapDialogAnswered()
            },
            onDismissRequest = {
                resetTrigger()
            }
        )
    else {
        homeActivity.requestWidgetPin()
        resetTrigger()
    }
}