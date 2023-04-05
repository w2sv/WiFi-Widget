package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.configcolumn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.HomeActivity
import com.w2sv.wifiwidget.ui.shared.DialogButton
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme

@Composable
internal fun ColorSelectionRow(
    modifier: Modifier = Modifier,
    viewModel: HomeActivity.ViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val customBackgroundColor by viewModel.customBackgroundColorState.collectAsState()
    val customLabelColor by viewModel.customLabelColorState.collectAsState()
    val customTextColor by viewModel.customTextColorState.collectAsState()

    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        WidgetColorConfigurator(stringResource(R.string.background), customBackgroundColor) {
            viewModel.showBackgroundColorPickerDialog.value = true
        }
        WidgetColorConfigurator(stringResource(R.string.labels), customLabelColor) {
            viewModel.showLabelColorPickerDialog.value = true
        }
        WidgetColorConfigurator(stringResource(R.string.other), customTextColor) {
            viewModel.showTextColorPickerDialog.value = true
        }
    }

    val showBackgroundColorPicker by viewModel.showBackgroundColorPickerDialog.collectAsState()
    val showLabelColorPicker by viewModel.showLabelColorPickerDialog.collectAsState()
    val showTextColorPicker by viewModel.showTextColorPickerDialog.collectAsState()

    if (showBackgroundColorPicker) {
        ColorPickerDialog(
            label = stringResource(R.string.background),
            applyColor = {
                viewModel.customBackgroundColorState.value = it
            },

        ) {
            viewModel.customBackgroundColorState.value = it
            viewModel.showBackgroundColorPickerDialog.value = false
        }
    }
    if (showLabelColorPicker) {
        ColorPickerDialog(label = stringResource(R.string.labels), applyColor = {
            viewModel.customLabelColorState.value = it
        }) {
            viewModel.customLabelColorState.value = it
            viewModel.showLabelColorPickerDialog.value = false
        }
    }
    if (showTextColorPicker) {
        ColorPickerDialog(label = stringResource(R.string.other), applyColor = {
            viewModel.customTextColorState.value = it
        })
    }
}

@Composable
private fun ColorPickerDialog(
    modifier: Modifier = Modifier,
    label: String,
    applyColor: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val controller = rememberColorPickerController()

    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = modifier, shape = RoundedCornerShape(20)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxHeight(0.2f), contentAlignment = Alignment.Center) {
                    JostText(
                        text = label,
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize
                    )
                }
                Box(modifier = Modifier.fillMaxHeight(0.6f), contentAlignment = Alignment.Center) {
                    HsvColorPicker(
                        modifier = Modifier.padding(8.dp),
                        controller = controller
                    )
                }
                Box(modifier = Modifier.fillMaxHeight(0.2f), contentAlignment = Alignment.Center) {
                    JostText(text = "RGBA()")
                }
                Row(
                    modifier = Modifier.fillMaxHeight(0.2f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DialogButton(onClick = onDismiss) {
                        JostText(text = stringResource(id = R.string.cancel))
                    }
                    DialogButton(onClick = {
                        applyColor(controller.selectedColor.value)
                        onDismiss()
                    }) {
                        JostText(text = stringResource(id = R.string.okay))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ColorPickerDialogPrev() {
    WifiWidgetTheme() {
        ColorPickerDialog(
            label = stringResource(R.string.background),
            applyColor = {}
        )
    }
}

@Composable
private fun WidgetColorConfigurator(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        JostText(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.margin_minimal))
        )
        Button(
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(containerColor = color),
            onClick = onClick,
            shape = CircleShape
        ) {}
    }
}