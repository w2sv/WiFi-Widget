package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colorpicker.model.ColorModel
import com.smarttoolfactory.colorpicker.picker.HSVColorPickerCircularWithSliders
import com.smarttoolfactory.colorpicker.widget.ColorComponentsDisplay
import com.w2sv.wifiwidget.ui.designsystem.ConfigurationDialog

@Composable
fun ColorPickerDialog(
    label: String,
    color: Color,
    setColor: (Color) -> Unit,
    appliedColor: Color,
    onDismissRequest: () -> Unit,
    applyColor: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    ConfigurationDialog(
        onDismissRequest = remember {
            {
                onDismissRequest()
            }
        },
        onApplyButtonPress = remember {
            {
                applyColor(color)
                onDismissRequest()
            }
        },
        modifier = modifier,
        columnModifier = Modifier
            .verticalScroll(rememberScrollState()),
        title = label,
        applyButtonEnabled = color != appliedColor
    ) {
        HSVColorPickerCircularWithSliders(
            initialColor = color,
            onColorChange = { newColor, _ -> setColor(newColor) },
        )
        ColorComponentsDisplay(
            color = color,
            colorModel = ColorModel.RGB,
            textColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(220.dp),
        )
    }
}

//@Preview
//@Composable
//private fun Prev() {
//    AppTheme {
//        ColorPickerDialog(
//            label = "Background",
//            appliedColor = Color.Red,
//            applyColor = {},
//            onDismissRequest = {},
//        )
//    }
//}
