package com.w2sv.wifiwidget.ui.home.configurationdialog

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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.extensions.requireCastActivity
import com.w2sv.androidutils.extensions.resetBoolean
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.activities.HomeActivity
import com.w2sv.wifiwidget.ui.home.LocationAccessPermissionDialog
import com.w2sv.wifiwidget.ui.home.model.LocationAccessPermissionDialogTrigger
import com.w2sv.wifiwidget.ui.shared.DialogButton
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme
import com.w2sv.wifiwidget.ui.shared.diagonalGradient

@Preview
@Composable
private fun WidgetConfigurationDialogPrev() {
    WifiWidgetTheme {
        WidgetConfigurationDialog(
            onDismiss = {},
            contentColumn = {
                ContentColumn(
                    selectedTheme = { 1 },
                    onSelectedTheme = {},
                    opacity = { 1f },
                    onOpacityChanged = {},
                    propertyChecked = { true },
                    onCheckedChange = { _, _ -> },
                    onInfoButtonClick = {}
                )
            },
            buttonRow = {
                ButtonRow(
                    onCancel = { /*TODO*/ },
                    onApply = { /*TODO*/ },
                    applyButtonEnabled = { true }
                )
            }
        )
    }
}

@Composable
fun StatefulWidgetConfigurationDialog(
    modifier: Modifier = Modifier,
    viewModel: HomeActivity.ViewModel = viewModel(),
    setInfoDialogPropertyIndex: (Int) -> Unit
) {
    val context = LocalContext.current
    val lapRequestLauncher = context.requireCastActivity<HomeActivity>().lapRequestLauncher

    /**
     * LocationAccessPermissionDialog
     */

    viewModel.lapDialogTrigger.collectAsState().apply {
        LocationAccessPermissionDialog {
            value
        }
    }

    val onDismiss: () -> Unit = {
        viewModel.widgetConfigurationStates.reset()
        viewModel.showWidgetConfigurationDialog.resetBoolean()
    }

    val theme by viewModel.widgetThemeState.collectAsState()
    val opacity by viewModel.widgetOpacityState.collectAsState()
    val applyButtonEnabled by viewModel.widgetConfigurationStates.requiringUpdate.collectAsState()

    WidgetConfigurationDialog(
        modifier = modifier,
        onDismiss = onDismiss,
        contentColumn = {
            ContentColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(260.dp, 460.dp),
                selectedTheme = {
                    theme
                },
                onSelectedTheme = {
                    viewModel.widgetThemeState.value = it
                },
                opacity = {
                    opacity
                },
                onOpacityChanged = {
                    viewModel.widgetOpacityState.value = it
                },
                propertyChecked = { property ->
                    viewModel.widgetPropertyStateMap.map.getValue(property)
                },
                onCheckedChange = { property, value ->
                    when {
                        property == "SSID" && value -> {
                            when (viewModel.lapDialogAnswered) {
                                false -> viewModel.lapDialogTrigger.value =
                                    LocationAccessPermissionDialogTrigger.SSIDCheck

                                true -> lapRequestLauncher.requestPermissionAndSetSSIDFlagCorrespondingly(
                                    viewModel
                                )
                            }
                        }

                        else -> viewModel.confirmAndSyncPropertyChange(property, value) {
                            context.showToast(R.string.uncheck_all_properties_toast)
                        }
                    }
                },
                onInfoButtonClick = {
                    setInfoDialogPropertyIndex(it)
                }
            )
        },
        buttonRow = {
            ButtonRow(
                onCancel = onDismiss,
                onApply = {
                    viewModel.widgetConfigurationStates.apply()
                    WifiWidgetProvider.triggerDataRefresh(context)
                    context.showToast(R.string.updated_widget_configuration)
                    viewModel.showWidgetConfigurationDialog.resetBoolean()
                },
                applyButtonEnabled = {
                    applyButtonEnabled
                },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    )
}

@Composable
private fun WidgetConfigurationDialog(
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
                        diagonalGradient(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                    .padding(vertical = 16.dp)
            ) {
                Icon(
                    painterResource(id = com.w2sv.widget.R.drawable.ic_settings_24),
                    contentDescription = "@null",
                    modifier = Modifier.padding(bottom = 12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                JostText(
                    text = stringResource(id = com.w2sv.widget.R.string.configure_widget),
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    fontWeight = FontWeight.Medium
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
    applyButtonEnabled: () -> Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DialogButton(onClick = onCancel) {
            JostText(text = stringResource(R.string.cancel))
        }
        DialogButton(onClick = onApply, enabled = applyButtonEnabled()) {
            JostText(text = stringResource(R.string.apply))
        }
    }
}