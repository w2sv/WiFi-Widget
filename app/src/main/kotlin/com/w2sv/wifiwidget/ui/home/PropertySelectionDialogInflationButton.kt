package com.w2sv.wifiwidget.ui.home

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.activities.HomeActivity
import com.w2sv.widget.WifiWidgetProvider

@Composable
fun PropertySelectionDialogInflationButton() {
    val viewModel: HomeActivity.ViewModel = viewModel()
    val context = LocalContext.current

    val propertyStatesDissimilar by viewModel.propertyStatesDissimilar.collectAsState()

    var inflateDialog by rememberSaveable {
        mutableStateOf(viewModel.openPropertiesConfigurationDialogOnStart)
    }

    if (inflateDialog) {
        PropertySelectionDialog(
            onCancel = {
                viewModel.resetWidgetPropertyStates()
                inflateDialog = false
            },
            onConfirm = {
                viewModel.syncWidgetPropertyStates()
                com.w2sv.widget.WifiWidgetProvider.refreshData(context)
                with(context) {
                    showToast(
                        getString(
                            if (com.w2sv.widget.WifiWidgetProvider.getWidgetIds(this).isNotEmpty())
                                R.string.updated_widget_properties
                            else
                                R.string.widget_properties_will_apply
                        )
                    )
                }
                inflateDialog = false
            },
            confirmButtonEnabled = propertyStatesDissimilar
        )
    }

    StatelessPropertySelectionDialogInflationButton {
        inflateDialog = true
    }
}

@Composable
private fun StatelessPropertySelectionDialogInflationButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Inflate widget properties configuration dialog",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}