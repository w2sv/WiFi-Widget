package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colorpicker.model.ColorModel
import com.smarttoolfactory.colorpicker.picker.HSVColorPickerCircularWithSliders
import com.smarttoolfactory.colorpicker.widget.ColorComponentsDisplay
import com.w2sv.wifiwidget.ui.designsystem.ConfigurationDialog
import com.w2sv.wifiwidget.ui.theme.AppTheme

private const val colorKey = "COLOR_KEY"

val colorSaver = Saver<Color, Int>(save = { it.toArgb() }, restore = { Color(it) })

@Composable
fun ColorPickerDialog(
    label: String,
    appliedColor: Color,
    onDismissRequest: () -> Unit,
    applyColor: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    var color by rememberSaveable(
        appliedColor,
        key = colorKey,
        stateSaver = colorSaver
    ) {
        mutableStateOf(
            appliedColor,
        )
    }

    ConfigurationDialog(
        onDismissRequest = onDismissRequest,
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
            onColorChange = { newColor, _ -> color = newColor },
        )
        ColorComponentsDisplay(
            color = color,
            colorModel = ColorModel.RGB,
            textColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(220.dp),
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
