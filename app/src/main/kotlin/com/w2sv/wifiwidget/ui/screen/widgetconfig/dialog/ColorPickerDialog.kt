package com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colorpicker.model.ColorModel
import com.smarttoolfactory.colorpicker.picker.HSVColorPickerCircularWithSliders
import com.smarttoolfactory.colorpicker.widget.ColorComponentsDisplay
import com.w2sv.wifiwidget.ui.designsystem.ConfigurationDialog

@Composable
fun ColorPickerDialog(
    data: WidgetConfigDialog.ColorPicker,
    updateColor: (Color) -> Unit,
    onDismissRequest: () -> Unit,
    applyColor: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ConfigurationDialog(
        onDismissRequest = onDismissRequest,
        onApplyButtonPress = {
            applyColor(data.color)
            onDismissRequest()
        },
        modifier = modifier,
        columnModifier = Modifier
            .verticalScroll(rememberScrollState()),
        title = stringResource(id = data.widgetColor.labelRes),
        applyButtonEnabled = data.hasBeenConfigured
    ) {
        val color = Color(data.color)
        HSVColorPickerCircularWithSliders(
            initialColor = color,
            onColorChange = { changedColor, _ -> updateColor(changedColor) }
        )
        ColorComponentsDisplay(
            color = color,
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
