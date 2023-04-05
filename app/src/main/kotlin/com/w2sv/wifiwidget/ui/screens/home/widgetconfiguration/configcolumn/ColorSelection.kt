package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.configcolumn

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.HomeActivity
import com.w2sv.wifiwidget.ui.shared.DialogButton
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme
import kotlin.math.roundToInt

enum class CustomizableSection {
    Background,
    Labels,
    Other
}

@Composable
internal fun ColorSelectionRow(
    modifier: Modifier = Modifier,
    viewModel: HomeActivity.ViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val customBackgroundColor by viewModel.customBackgroundColorState.collectAsState()
    val customLabelColor by viewModel.customLabelsColorState.collectAsState()
    val customTextColor by viewModel.customOtherColorState.collectAsState()

    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        WidgetColorConfigurator(stringResource(R.string.background), customBackgroundColor) {
            viewModel.customizationDialogSection.value = CustomizableSection.Background
        }
        WidgetColorConfigurator(stringResource(R.string.labels), customLabelColor) {
            viewModel.customizationDialogSection.value = CustomizableSection.Labels
        }
        WidgetColorConfigurator(stringResource(R.string.other), customTextColor) {
            viewModel.customizationDialogSection.value = CustomizableSection.Other
        }
    }

    viewModel.customizationDialogSection.collectAsState().value?.let { section ->
        ColorPickerDialog(
            properties = when (section) {
                CustomizableSection.Background -> Properties(R.string.background) {
                    viewModel.customBackgroundColorState.value = it
                }

                CustomizableSection.Labels -> Properties(R.string.labels) {
                    viewModel.customLabelsColorState.value = it
                }

                CustomizableSection.Other -> Properties(R.string.other) {
                    viewModel.customOtherColorState.value = it
                }
            }
        )
    }
}

@Stable
private data class Properties(@StringRes val labelRes: Int, val applyColor: (Color) -> Unit)

@Composable
private fun ColorPickerDialog(
    properties: Properties,
    modifier: Modifier = Modifier,
    viewModel: HomeActivity.ViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val controller = rememberColorPickerController()
    var color by remember(properties.hashCode()) {
        mutableStateOf(controller.selectedColor.value)
    }

    Dialog(onDismissRequest = viewModel::onDismissCustomizationDialog) {
        ElevatedCard(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.elevatedCardElevation(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                JostText(
                    text = stringResource(id = properties.labelRes),
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize
                )
                HsvColorPicker(
                    modifier = Modifier
                        .padding(8.dp)
                        .height(300.dp),
                    controller = controller,
                    onColorChanged = {
                        color = it.color
                    }
                )
                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .height(36.dp),
                    controller = controller
                )
                Spacer(modifier = Modifier.padding(vertical = 12.dp))
                JostText(
                    text = "RGB(${color.red.toRGBInt()}, ${color.green.toRGBInt()}, ${color.blue.toRGBInt()})",
                    color = color
                )
                Spacer(modifier = Modifier.padding(vertical = 12.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DialogButton(onClick = viewModel::onDismissCustomizationDialog) {
                        JostText(text = stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 12.dp))
                    DialogButton(onClick = {
                        properties.applyColor(controller.selectedColor.value)
                        viewModel.onDismissCustomizationDialog()
                    }) {
                        JostText(text = stringResource(id = R.string.okay))
                    }
                }
            }
        }
    }
}

private fun Float.toRGBInt(): Int = (this * 255).roundToInt()

@Preview
@Composable
private fun ColorPickerDialogPrev() {
    WifiWidgetTheme {
        ColorPickerDialog(
            properties = Properties(R.string.background) {}
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