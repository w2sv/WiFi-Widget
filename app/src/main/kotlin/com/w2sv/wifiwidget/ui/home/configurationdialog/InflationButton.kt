package com.w2sv.wifiwidget.ui.home.configurationdialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.wifiwidget.activities.HomeActivity

@Composable
fun StatefulWidgetConfigurationDialogButton(
    modifier: Modifier = Modifier,
    viewModel: HomeActivity.ViewModel = viewModel()
) {
    WidgetConfigurationDialogButton(modifier) {
        viewModel.openConfigurationDialog.value = true
    }

    val inflateDialog by viewModel.openConfigurationDialog.collectAsState()

    if (inflateDialog) {
        StatefulWidgetConfigurationDialog {
            viewModel.openConfigurationDialog.value = false
        }
    }
}

@Composable
private fun WidgetConfigurationDialogButton(
    modifier: Modifier,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Press to inflate widget configuration dialog.",
            modifier = modifier,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}