package com.w2sv.wifiwidget.ui.screens.home.locationaccesspermission

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.coroutines.reset
import com.w2sv.common.data.PreferencesKey
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.HomeScreenViewModel
import com.w2sv.wifiwidget.ui.components.DialogButton
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.theme.AppTheme

enum class LocationAccessPermissionRequestTrigger {
    PinWidgetButtonPress,
    SSIDCheck
}

@Composable
fun LocationAccessPermissionRational(
    trigger: LocationAccessPermissionRequestTrigger,
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = viewModel()
) {
    LocationAccessPermissionRational(
        onConfirmButtonClick = {
            viewModel.saveToDataStore(
                PreferencesKey.LOCATION_ACCESS_PERMISSION_RATIONAL_SHOWN,
                true
            )
            viewModel.lapRationalTrigger.reset()
            viewModel.lapRequestTrigger.value = trigger
        },
        onDismissRequest = { viewModel.lapRequestTrigger.value = trigger },
        modifier = modifier
    )
}

@Composable
fun LocationAccessPermissionRational(
    onConfirmButtonClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        icon = {
            Icon(
                Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            JostText(
                text = stringResource(id = R.string.location_access_permission),
                textAlign = TextAlign.Center
            )
        },
        text = {
            JostText(
                text = stringResource(id = R.string.lap_dialog_text),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            DialogButton(
                onClick = onConfirmButtonClick,
                modifier = Modifier.fillMaxWidth()
            ) { JostText(text = stringResource(R.string.proceed)) }
        },
        onDismissRequest = onDismissRequest
    )
}


@Preview
@Composable
private fun Prev() {
    AppTheme {
        LocationAccessPermissionRational({}, {})
    }
}