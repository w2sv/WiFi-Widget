package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.configcolumn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.smarttoolfactory.colorpicker.picker.ColorPickerCircleValueHSV
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
    var color by remember {
        mutableStateOf(Color(viewModel.customWidgetColorsState[properties.label]!!))
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
                ColorPickerCircleValueHSV(
                    initialColor = color,
                    onColorChange = { newColor, _ -> color = newColor })
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
                        viewModel.customWidgetColorsState[properties.label] = color.toArgb()
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