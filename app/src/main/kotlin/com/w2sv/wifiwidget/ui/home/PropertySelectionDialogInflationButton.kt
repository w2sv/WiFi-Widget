package com.w2sv.wifiwidget.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.wifiwidget.activities.HomeActivity

@Composable
fun PropertySelectionDialogInflationButton(
    modifier: Modifier = Modifier,
    viewModel: HomeActivity.ViewModel = viewModel()
) {
    var inflateDialog by rememberSaveable {
        mutableStateOf(viewModel.openPropertiesConfigurationDialogOnStart)
    }

    StatelessPropertySelectionDialogInflationButton(modifier) {
        inflateDialog = true
    }

    if (inflateDialog)
        PropertySelectionDialog {
            inflateDialog = false
        }
}

@Composable
private fun StatelessPropertySelectionDialogInflationButton(
    modifier: Modifier,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Inflate widget properties configuration dialog",
            modifier = modifier,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}