package com.w2sv.wifiwidget.ui.screen.locationaccess.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.location.capability.access.LocationAccessCapability
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessScreenTokens
import com.w2sv.wifiwidget.ui.screen.locationaccess.flow.LocationAccessEffect
import com.w2sv.wifiwidget.ui.screen.locationaccess.flow.LocationAccessEvent
import com.w2sv.wifiwidget.ui.screen.locationaccess.launchLocationAccessEffect
import com.w2sv.wifiwidget.ui.util.backgroundLocationRequiresSettings

@Composable
internal fun BackgroundLocationDisclosure(capability: LocationAccessCapability, onEvent: (LocationAccessEvent) -> LocationAccessEffect?) {
    val context = LocalContext.current
    val backgroundOptionLabel = if (backgroundLocationRequiresSettings) {
        context.packageManager.backgroundPermissionOptionLabel.toString()
    } else {
        null
    }

    BackgroundLocationDisclosure(
        backgroundOptionLabel = backgroundOptionLabel,
        onContinue = {
            val effect = onEvent(RequestBackground)
            capability.launchLocationAccessEffect(effect)
        },
        onDismiss = { onEvent(DismissBackgroundDisclosure) }
    )
}

@Composable
private fun BackgroundLocationDisclosure(
    backgroundOptionLabel: String?,
    onContinue: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.background_location_access)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(LocationAccessScreenTokens.disclosureTextSpacing)) {
                Text(stringResource(R.string.background_location_disclosure))
                if (backgroundOptionLabel != null) {
                    Text(stringResource(R.string.background_location_settings_instruction, backgroundOptionLabel))
                }
            }
        },
        confirmButton = { TextButton(onClick = onContinue) { Text(stringResource(R.string.continue_action)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.not_now)) } }
    )
}
