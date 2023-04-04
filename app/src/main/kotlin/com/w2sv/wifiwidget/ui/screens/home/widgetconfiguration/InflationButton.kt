package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration

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
import com.w2sv.androidutils.extensions.reset
import com.w2sv.wifiwidget.ui.screens.home.HomeActivity
import com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.configcolumn.PropertyInfoDialog

@Composable
fun StatefulWidgetConfigurationDialogButton(
    modifier: Modifier = Modifier,
    viewModel: HomeActivity.ViewModel = viewModel()
) {
    WidgetConfigurationDialogButton(modifier) {
        viewModel.showWidgetConfigurationDialog.value = true
    }

    val inflateDialog by viewModel.showWidgetConfigurationDialog.collectAsState()
    val propertyInfoDialogIndex by viewModel.propertyInfoDialogIndex.collectAsState()

    if (inflateDialog) {
        WidgetConfigurationDialog()

        propertyInfoDialogIndex?.let {
            PropertyInfoDialog(it) {
                viewModel.propertyInfoDialogIndex.reset()
            }
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
            contentDescription = "Inflate the widget configuration dialog.",
            modifier = modifier,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}