package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colorpicker.model.ColorModel
import com.smarttoolfactory.colorpicker.picker.HSVColorPickerCircularWithSliders
import com.smarttoolfactory.colorpicker.widget.ColorComponentsDisplay
import com.w2sv.wifiwidget.ui.designsystem.ConfigurationDialog
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.ColorPickerDialogData

@Composable
fun ColorPickerDialog(
    properties: ColorPickerDialogData,
    onDismissRequest: () -> Unit,
    applyColor: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    ConfigurationDialog(
        onDismissRequest = onDismissRequest,
        onApplyButtonPress = remember {
            {
                applyColor(properties.color)
                onDismissRequest()
            }
        },
        modifier = modifier,
        columnModifier = Modifier
            .verticalScroll(rememberScrollState()),
        title = stringResource(id = properties.customWidgetColor.labelRes),
        applyButtonEnabled = properties.colorsDissimilar
    ) {
        HSVColorPickerCircularWithSliders(
            initialColor = properties.color,
            onColorChange = remember { { newColor, _ -> properties.color = newColor } }
        )
        ColorComponentsDisplay(
            color = properties.color,
            colorModel = ColorModel.RGB,
            textColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(220.dp)
        )
    }
}

// @Preview
// @Composable
// private fun Prev() {
//    AppTheme {
//        ColorPickerDialog(
//            label = "Background",
//            appliedColor = Color.Red,
//            applyColor = {},
//            onDismissRequest = {},
//        )
//    }
// }
