package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.configcolumn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.w2sv.common.extensions.toRGBInt
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.HomeActivity
import com.w2sv.wifiwidget.ui.shared.DialogButton
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme

@Stable
internal data class Properties(val label: String)

@Composable
internal fun ColorPickerDialog(
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
                    text = properties.label,
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
                        viewModel.customWidgetColorsState[properties.label] =
                            controller.selectedColor.value.toArgb()
                        viewModel.onDismissCustomizationDialog()
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
private fun Prev() {
    WifiWidgetTheme {
        ColorPickerDialog(
            properties = Properties(stringResource(id = R.string.background))
        )
    }
}