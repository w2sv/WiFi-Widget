package com.w2sv.wifiwidget.ui.home.widgetconfiguration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.extensions.requireCastActivity
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.activities.HomeActivity
import com.w2sv.wifiwidget.ui.DialogButton
import com.w2sv.wifiwidget.ui.JostText
import com.w2sv.wifiwidget.ui.WifiWidgetTheme
import com.w2sv.wifiwidget.ui.home.LocationAccessPermissionDialog
import com.w2sv.wifiwidget.ui.home.LocationAccessPermissionDialogTrigger
import com.w2sv.wifiwidget.ui.home.PropertyInfoDialog

@Preview
@Composable
private fun StatelessWidgetConfigurationDialogPrev() {
    WifiWidgetTheme {
        StatelessWidgetConfigurationDialog(
            onDismiss = {},
            contentColumn = {
                ConfigurationColumn(
                    selectedThemeIndex = { 1 },
                    onSelectedThemeIndex = {},
                    propertyChecked = { true },
                    onCheckedChange = { _, _ -> },
                    onInfoButtonClick = {}
                )
            },
            buttonRow = {
                ButtonRow(
                    onCancel = { /*TODO*/ },
                    onApply = { /*TODO*/ },
                    confirmButtonEnabled = { true }
                )
            }
        )
    }
}

@Composable
fun WidgetConfigurationDialog(
    modifier: Modifier = Modifier,
    viewModel: HomeActivity.ViewModel = viewModel(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context.requireCastActivity<HomeActivity>()

    /**
     * PropertyInfoDialog
     */

    var infoDialogPropertyIndex by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    infoDialogPropertyIndex?.let {
        PropertyInfoDialog(it) {
            infoDialogPropertyIndex = null
        }
    }

    /**
     * LocationAccessPermissionDialog
     */

    var showLocationAccessPermissionDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showLocationAccessPermissionDialog)
        LocationAccessPermissionDialog(trigger = LocationAccessPermissionDialogTrigger.SSIDCheck) {
            showLocationAccessPermissionDialog = false
        }

    val _onDismiss: () -> Unit = {
        viewModel.resetWidgetConfiguration()
        onDismiss()
    }

    val selectedThemeIndex by viewModel.widgetTheme.collectAsState()
    val widgetConfigurationRequiringUpdate by viewModel.widgetConfigurationRequiringUpdate.collectAsState()

    StatelessWidgetConfigurationDialog(
        modifier = modifier,
        onDismiss = _onDismiss,
        contentColumn = {
            ConfigurationColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(260.dp, 460.dp),
                selectedThemeIndex = {
                    selectedThemeIndex
                },
                onSelectedThemeIndex = {
                    viewModel.widgetTheme.value = it
                },
                propertyChecked = { property ->
                    viewModel.widgetPropertyFlags.getValue(property)
                },
                onCheckedChange = { property, newValue ->
                    when {
                        property == viewModel.ssidKey && newValue -> {
                            when (viewModel.lapDialogAnswered) {
                                false -> showLocationAccessPermissionDialog = true
                                true -> activity.lapRequestLauncher.requestPermissionIfRequired(
                                    onDenied = { viewModel.setSSIDState(false) },
                                    onGranted = { viewModel.setSSIDState(true) }
                                )
                            }
                        }

                        !viewModel.setWidgetPropertyFlag(property, newValue) -> {
                            context.showToast(R.string.uncheck_all_properties_toast)
                        }
                    }
                },
                onInfoButtonClick = { propertyIndex ->
                    infoDialogPropertyIndex = propertyIndex
                }
            )
        },
        buttonRow = {
            ButtonRow(
                onCancel = _onDismiss,
                onApply = {
                    viewModel.updateWidgetConfiguration()
                    WifiWidgetProvider.triggerDataRefresh(context)
                    context.showToast(R.string.updated_widget_configuration)
                    onDismiss()
                },
                confirmButtonEnabled = {
                    widgetConfigurationRequiringUpdate
                },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    )
}

@Composable
private fun StatelessWidgetConfigurationDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    contentColumn: @Composable ColumnScope.(Modifier) -> Unit,
    buttonRow: @Composable ColumnScope.(Modifier) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        ElevatedCard(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.elevatedCardElevation(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    // gradient background
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary
                            ),
                            start = Offset(0f, Float.POSITIVE_INFINITY),
                            end = Offset(Float.POSITIVE_INFINITY, 0f)
                        )
                    )
                    .padding(vertical = 16.dp)
            ) {
                JostText(
                    text = stringResource(id = com.w2sv.widget.R.string.configure_widget),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Divider(
                    Modifier.padding(horizontal = 22.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                contentColumn(Modifier)
                buttonRow(Modifier)
            }
        }
    }
}

@Composable
private fun ButtonRow(
    onCancel: () -> Unit,
    onApply: () -> Unit,
    confirmButtonEnabled: () -> Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DialogButton(onClick = onCancel) {
            JostText(text = stringResource(R.string.cancel))
        }
        DialogButton(onClick = onApply, enabled = confirmButtonEnabled()) {
            JostText(text = stringResource(R.string.apply))
        }
    }
}