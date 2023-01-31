package com.w2sv.wifiwidget.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.extensions.goToWebpage
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.JostText
import com.w2sv.wifiwidget.ui.WifiWidgetTheme
import com.w2sv.wifiwidget.widget.WifiWidgetProvider

@Composable
fun PropertiesConfigurationDialogInflationButton() {
    val viewModel: HomeActivity.ViewModel = viewModel()
    val context = LocalContext.current

    var triggerOnClickListener by rememberSaveable {
        mutableStateOf(viewModel.openPropertiesConfigurationDialogOnStart)
    }

    if (triggerOnClickListener)
        PropertiesConfigurationDialog {
            triggerOnClickListener = false
            if (viewModel.syncWidgetProperties()) {
                WifiWidgetProvider.refreshData(context)
                context.showToast(context.getString(R.string.updated_widget_properties))
            }
        }

    IconButton(onClick = { triggerOnClickListener = true }) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Inflate widget properties configuration dialog",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PropertiesConfigurationDialog(onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        ElevatedCard(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.elevatedCardElevation(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                // gradient background
                modifier = Modifier.background(
                    Brush.linearGradient(
                        listOf(
                            colorResource(id = R.color.mischka_dark),
                            colorResource(id = R.color.mischka)
                        ),
                        start = Offset(0f, Float.POSITIVE_INFINITY),
                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                    )
                )
            ) {
                JostText(
                    text = stringResource(id = R.string.configure_properties),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(
                        top = 24.dp,
                        bottom = 12.dp,
                        start = 18.dp,
                        end = 18.dp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Divider(
                    Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                WidgetPropertiesSelectionColumn()
            }
        }
    }
}

@Composable
private fun ColumnScope.WidgetPropertiesSelectionColumn() {
    val viewModel: HomeActivity.ViewModel = viewModel()
    val context = LocalContext.current

    var infoDialogProperty by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val propertyInfoMap = stringArrayResource(id = R.array.widget_properties)
        .zip(
            stringArrayResource(id = R.array.property_info)
                .zip(stringArrayResource(id = R.array.property_urls))
        )
        .toMap()

    infoDialogProperty?.let {
        propertyInfoMap.getValue(it).run {
            PropertyInfoDialog(label = it, text = first, url = second) {
                infoDialogProperty = null
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 26.dp)
            .verticalScroll(rememberScrollState())
            .weight(1f, fill = false)
    ) {
        stringArrayResource(id = R.array.widget_properties)
            .forEach { widgetProperty ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    JostText(
                        text = widgetProperty,
                        modifier = Modifier.weight(1f, fill = true),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp
                    )
                    Checkbox(
                        checked = viewModel.widgetPropertyStates.getValue(widgetProperty),
                        onCheckedChange = {
                            if (viewModel.unchecksAllProperties(it, widgetProperty))
                                context.showToast(context.getString(R.string.uncheck_all_properties_toast))
                            else
                                viewModel.widgetPropertyStates[widgetProperty] = it
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    IconButton(onClick = {
                        infoDialogProperty = widgetProperty
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
private fun PropertyInfoDialog(
    label: String,
    text: String,
    url: String,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            ElevatedButton(
                onClick = onDismissRequest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.elevatedButtonElevation(8.dp)
            ) {
                JostText(text = "Close")
            }
        },
        title = {
            JostText(
                text = label,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                Modifier
                    .sizeIn(maxHeight = 520.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                Arrangement.Center,
                Alignment.CenterHorizontally
            ) {
                JostText(text = text, textAlign = TextAlign.Center)
                ElevatedButton(
                    onClick = {
                        context.goToWebpage(url)
                        onDismissRequest()
                    },
                    modifier = Modifier.padding(top = 20.dp, bottom = 12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.elevatedButtonElevation(8.dp)
                ) {
                    JostText(text = "Learn more")
                }
            }
        }
    )
}

@Preview
@Composable
fun Preview() {
    WifiWidgetTheme {
        PropertyInfoDialog(label = "IP", text = "Some Text", "") {

        }
    }
}