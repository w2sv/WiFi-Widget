package com.w2sv.wifiwidget.ui.home.configurationdialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.wifiwidget.activities.HomeActivity
import com.w2sv.wifiwidget.ui.home.configurationdialog.content.PropertyInfoDialog
import com.w2sv.wifiwidget.ui.shared.InfoDialog

@Composable
fun StatefulWidgetConfigurationDialogButton(
    modifier: Modifier = Modifier,
    viewModel: HomeActivity.ViewModel = viewModel()
) {
    WidgetConfigurationDialogButton(modifier) {
        viewModel.showWidgetConfigurationDialog.value = true
    }

    val inflateDialog by viewModel.showWidgetConfigurationDialog.collectAsState()

    var infoDialogPropertyIndex by rememberSaveable {
        mutableStateOf<Int?>(null)
    }
    val (showRefreshingInfoDialog, setShowRefreshingInfoDialog) = rememberSaveable {
        mutableStateOf(false)
    }

    if (inflateDialog) {
        WidgetConfigurationDialog(
            setInfoDialogPropertyIndex = {
                infoDialogPropertyIndex = it
            },
            showRefreshingInfoDialog = {
                setShowRefreshingInfoDialog(true)
            }
        )

        infoDialogPropertyIndex?.let {
            PropertyInfoDialog(it) {
                infoDialogPropertyIndex = null
            }
        }
        if (showRefreshingInfoDialog) {
            InfoDialog(title = "Data Refreshing", text = "BlubBlub") {
                setShowRefreshingInfoDialog(false)
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