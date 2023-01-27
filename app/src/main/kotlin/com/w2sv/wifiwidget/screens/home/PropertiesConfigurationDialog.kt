package com.w2sv.wifiwidget.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.JostText
import com.w2sv.wifiwidget.widget.WifiWidgetProvider

@Preview
@Composable
fun PropertiesConfigurationDialogInflationButton() {
    val viewModel: HomeActivity.ViewModel = viewModel()
    val context = LocalContext.current
    var triggerOnClickListener by rememberSaveable {
        mutableStateOf(false)
    }

    if (triggerOnClickListener)
        PropertiesConfigurationDialog {
            triggerOnClickListener = false
            if (viewModel.syncWidgetProperties()){
                WifiWidgetProvider.refreshData(context)
                context.showToast("Updated widget properties")
            }
        }

    IconButton(onClick = { triggerOnClickListener = true }) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Inflate widget properties selection dialog",
            modifier = Modifier.size(32.dp),
            tint = colorResource(id = R.color.blue_chill)
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
                    text = "Configure properties",
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(
                        top = 24.dp,
                        bottom = 12.dp,
                        start = 18.dp,
                        end = 18.dp
                    ),
                    color = colorResource(id = R.color.blue_chill_dark)
                )
                Divider(Modifier.padding(horizontal = 22.dp, vertical = 12.dp), color = Color.White)
                WidgetPropertiesSelectionColumn()
            }
        }
    }
}

@Composable
private fun ColumnScope.WidgetPropertiesSelectionColumn(
) {
    val viewModel: HomeActivity.ViewModel = viewModel()
    val context = LocalContext.current

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
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Checkbox(
                        checked = viewModel.widgetPropertyStates.getValue(widgetProperty),
                        onCheckedChange = {
                            if (viewModel.unchecksAllProperties(it, widgetProperty))
                                context.showToast("You have to leave at least one property checked!")
                            else
                                viewModel.widgetPropertyStates[widgetProperty] = it
                        },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
    }
}