package com.w2sv.wifiwidget.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.JostText
import com.w2sv.wifiwidget.ui.WifiWidgetTopBar
import com.w2sv.wifiwidget.widget.WifiWidgetProvider
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
internal fun HomeScreen() {
    val coroutineScope = rememberCoroutineScope()

    NavigationDrawer { drawerState ->
        Scaffold(topBar = {
            WifiWidgetTopBar {
                coroutineScope.launch {
                    drawerState.open()
                }
            }
        }) { paddingValues ->
            Column(
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                Arrangement.SpaceBetween,
                Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(1.5f))
                Box(Modifier.weight(0.5f)) {
                    PinWidgetButton()
                }
                Spacer(Modifier.weight(0.5f))
                Box(Modifier.weight(1f)) {
                    PropertySelectionDialogInflationButton()
                }
                CopyrightText(modifier = Modifier.padding(bottom = dimensionResource(R.dimen.margin_minimal)))
            }
        }
    }
}

@Composable
private fun PropertySelectionDialogInflationButton() {
    val viewModel: HomeActivity.ViewModel = viewModel()
    val context = LocalContext.current

    val propertyStatesDissimilar by viewModel.propertyStatesDissimilar.collectAsState()

    var inflateDialog by rememberSaveable {
        mutableStateOf(viewModel.openPropertiesConfigurationDialogOnStart)
    }

    if (inflateDialog) {
        PropertiesConfigurationDialog(
            onCancel = {
                viewModel.resetWidgetPropertyStates()
                inflateDialog = false
            },
            onConfirm = {
                viewModel.syncWidgetPropertyStates()
                WifiWidgetProvider.refreshData(context)
                context.showToast(context.getString(R.string.updated_widget_properties))
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

@Composable
private fun CopyrightText(modifier: Modifier) {
    JostText(
        text = "© 2022 - ${Calendar.getInstance().get(Calendar.YEAR)} | W2SV",
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 16.sp,
        modifier = modifier
    )
}