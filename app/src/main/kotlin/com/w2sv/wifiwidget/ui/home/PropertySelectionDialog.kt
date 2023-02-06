package com.w2sv.wifiwidget.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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

@Preview
@Composable
private fun StatelessPropertySelectionDialogPrev() {
    WifiWidgetTheme {
        StatelessPropertySelectionDialog(
            onCancel = { /*TODO*/ },
            onConfirm = { /*TODO*/ },
            confirmButtonEnabled = true
        ) {
            StatelessPropertyRows({ true }, { _, _ -> }, {})
        }
    }
}

@Composable
fun PropertySelectionDialog(
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

    StatelessPropertySelectionDialog(
        modifier = modifier,
        onCancel = {
            viewModel.resetWidgetPropertyStates()
            onDismiss()
        },
        onConfirm = {
            viewModel.syncWidgetPropertyStates()
            WifiWidgetProvider.triggerDataRefresh(context)
            context.showToast(
                if (WifiWidgetProvider.getWidgetIds(context).isNotEmpty())
                    R.string.updated_widget_properties
                else
                    R.string.widget_properties_will_apply
            )
            onDismiss()
        },
        confirmButtonEnabled = viewModel.propertyStatesDissimilar.collectAsState().value
    ) {
        StatelessPropertyRows(
            propertyChecked = { property ->
                viewModel.widgetPropertyStates.getValue(property)
            },
            onCheckedChange = { property, newValue ->
                when {
                    property == viewModel.ssidKey && newValue -> {
                        when (viewModel.lapDialogAnswered) {
                            false -> showLocationAccessPermissionDialog = true
                            true -> activity.launchLAPRequestIfRequired(viewModel)
                        }
                    }
                    !viewModel.onChangePropertyState(property, newValue) -> {
                        with(context) {
                            showToast(getString(R.string.uncheck_all_properties_toast))
                        }
                    }
                }
            },
            onInfoButtonClick = { propertyIndex ->
                infoDialogPropertyIndex = propertyIndex
            }
        )
    }
}

private fun HomeActivity.launchLAPRequestIfRequired(viewModel: HomeActivity.ViewModel) {
    lapRequestLauncher.requestPermissionIfRequired(
        onDenied = { viewModel.setSSIDState(false) },
        onGranted = { viewModel.setSSIDState(true) }
    )
}

@Composable
private fun StatelessPropertySelectionDialog(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    confirmButtonEnabled: Boolean,
    propertyRows: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        ElevatedCard(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.elevatedCardElevation(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                // gradient background
                modifier = Modifier
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

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(260.dp, 460.dp)
                        .padding(vertical = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SubHeader("Theme", Modifier.padding(top = 12.dp, bottom = 22.dp))
                    ThemeSelectionRow(modifier = Modifier.fillMaxWidth())

                    SubHeader("Displayed Properties", Modifier.padding(vertical = 22.dp))
                    propertyRows()
                }

                ButtonRow(
                    onCancel = onCancel,
                    onConfirm = onConfirm,
                    confirmButtonEnabled = confirmButtonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SubHeader(text: String, modifier: Modifier = Modifier) {
    JostText(
        text = text,
        modifier = modifier,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.inversePrimary
    )
}

@Composable
private fun ThemeSelectionRow(modifier: Modifier = Modifier) {
    var selectedThemeIndex by rememberSaveable {
        mutableStateOf(1)
    }

    Row(
        modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        remember {
            listOf(
                ThemeIndicatorProperties(label = "Light", color = Color.White),
                ThemeIndicatorProperties(label = "Device Default", color = Color.Gray),
                ThemeIndicatorProperties(label = "Dark", color = Color.Black)
            )
        }
            .forEachIndexed { index, properties ->
                ThemeIndicator(
                    properties = properties,
                    selected = index == selectedThemeIndex,
                    modifier = Modifier.padding(
                        horizontal = 16.dp
                    )
                ) {
                    selectedThemeIndex = index
                }
            }
    }
}

private data class ThemeIndicatorProperties(val label: String, val color: Color)

@Composable
private fun ThemeIndicator(
    properties: ThemeIndicatorProperties,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        JostText(
            text = properties.label,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.margin_minimal))
        )
        ElevatedButton(
            onClick,
            modifier = Modifier
                .size(36.dp),
            shape = CircleShape,
            colors = ButtonDefaults.elevatedButtonColors(containerColor = properties.color),
            border = if (selected)
                BorderStroke(3.dp, colorResource(id = com.w2sv.resources.R.color.blue_chill))
            else
                BorderStroke(Dp.Hairline, Color.Black)
        ) {}
    }
}

@Composable
private fun StatelessPropertyRows(
    propertyChecked: (String) -> Boolean,
    onCheckedChange: (String, Boolean) -> Unit,
    onInfoButtonClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 26.dp)
    ) {
        stringArrayResource(id = R.array.wifi_properties)
            .forEachIndexed { propertyIndex, property ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    JostText(
                        text = property,
                        modifier = Modifier.weight(1f, fill = true),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp
                    )
                    Checkbox(
                        checked = propertyChecked(property),
                        onCheckedChange = { onCheckedChange(property, it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    IconButton(onClick = {
                        onInfoButtonClick(propertyIndex)
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Click to toggle the property info dialog",
                            modifier = Modifier.size(
                                dimensionResource(id = R.dimen.size_icon)
                            ),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
    }
}

@Composable
private fun ButtonRow(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    confirmButtonEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DialogButton(onClick = onCancel) {
            JostText(text = stringResource(R.string.cancel))
        }
        DialogButton(onClick = onConfirm, enabled = confirmButtonEnabled) {
            JostText(text = stringResource(R.string.apply))
        }
    }
}