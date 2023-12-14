package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components.colors

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colorpicker.model.ColorModel
import com.smarttoolfactory.colorpicker.picker.HSVColorPickerCircularWithSliders
import com.smarttoolfactory.colorpicker.widget.ColorComponentsDisplay
import com.w2sv.wifiwidget.ui.components.CustomDialog
import com.w2sv.wifiwidget.ui.components.DialogButtonRow
import com.w2sv.wifiwidget.ui.components.DialogHeaderProperties
import com.w2sv.wifiwidget.ui.theme.AppTheme

@Composable
fun ColorPickerDialog(
    label: String,
    appliedColor: Color,
    onDismissRequest: () -> Unit,
    applyColor: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    var color by remember(appliedColor) {
        mutableStateOf(
            appliedColor,
        )
    }

    CustomDialog(
        headerProperties = DialogHeaderProperties(title = label),
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        scrollState = rememberScrollState(),
    ) {
        HSVColorPickerCircularWithSliders(
            initialColor = color,
            onColorChange = { newColor, _ -> color = newColor },
        )
        ColorComponentsDisplay(
            color = color,
            colorModel = ColorModel.RGB,
            textColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(220.dp),
        )
        DialogButtonRow(
            onCancel = onDismissRequest,
            onApply = {
                applyColor(color)
                onDismissRequest()
            },
            modifier = Modifier
                .padding(vertical = 16.dp),
        )
    }
}

@Preview
@Composable
private fun Prev() {
    AppTheme {
        ColorPickerDialog(
            label = "Background",
            appliedColor = Color.Red,
            applyColor = {},
            onDismissRequest = {},
        )
    }
}
