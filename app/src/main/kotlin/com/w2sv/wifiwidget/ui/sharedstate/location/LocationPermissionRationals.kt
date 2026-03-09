package com.w2sv.wifiwidget.ui.sharedstate.location

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.w2sv.composed.core.rememberStyledTextResource
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.LocalLocationAccessCapability
import com.w2sv.wifiwidget.ui.designsystem.DialogButton
import com.w2sv.wifiwidget.ui.designsystem.HighlightedDialogButton
import com.w2sv.wifiwidget.ui.designsystem.InfoIcon

@Composable
fun LocationAccessRationals() {
    val capability = LocalLocationAccessCapability.current

    when {
        capability.showForegroundRational -> LocationAccessPermissionRational(
            onProceed = capability::onForegroundRationalProceed
        )

        capability.showBackgroundRational -> BackgroundLocationAccessRational(
            launchPermissionRequest = capability::launchBackgroundPermission,
            onDismissRequest = capability::dismissBackgroundRational
        )
    }
}

@Composable
private fun LocationAccessPermissionRational(onProceed: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        modifier = modifier,
        icon = { InfoIcon() },
        text = {
            Text(
                text = rememberStyledTextResource(id = R.string.location_access_permission_rational),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            DialogButton(
                text = stringResource(R.string.understood),
                onClick = onProceed,
                modifier = Modifier.fillMaxWidth()
            )
        },
        onDismissRequest = onProceed
    )
}

@Composable
private fun BackgroundLocationAccessRational(launchPermissionRequest: () -> Unit, onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            HighlightedDialogButton(
                text = stringResource(id = R.string.grant),
                onClick = {
                    launchPermissionRequest()
                    onDismissRequest()
                }
            )
        },
        dismissButton = { DialogButton(text = stringResource(id = R.string.maybe_later), onClick = onDismissRequest) },
        icon = { InfoIcon() },
        text = { Text(text = rememberStyledTextResource(id = R.string.background_location_access_rational)) }
    )
}
