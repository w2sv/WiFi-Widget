package com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.core.resources.R
import com.w2sv.domain.wifiproperty.AccessPointNames
import com.w2sv.wifiwidget.ui.designsystem.ConfigurationDialog

/**
 * Edits the name assigned to an access point. The BSSID is editable only if [AccessPointNameEditing.isBssidEditable].
 */
@Composable
fun AccessPointNameDialog(
    data: WidgetConfigDialog.AccessPointNameEditing,
    applyName: (bssid: String, name: String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var bssid by rememberSaveable { mutableStateOf(data.bssid) }
    var name by rememberSaveable { mutableStateOf(data.name) }
    val isBssidValid = AccessPointNames.normalizeBssid(bssid) != null

    ConfigurationDialog(
        onDismissRequest = onDismissRequest,
        onApplyButtonPress = {
            applyName(bssid, name)
            onDismissRequest()
        },
        applyButtonEnabled = isBssidValid && name.isNotBlank(),
        title = stringResource(R.string.edit_access_point_name),
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = bssid,
                onValueChange = { bssid = it },
                label = { Text(stringResource(R.string.bssid)) },
                enabled = data.isBssidEditable,
                isError = !isBssidValid,
                supportingText = if (!isBssidValid) {
                    { Text(stringResource(R.string.invalid_bssid)) }
                } else {
                    null
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
