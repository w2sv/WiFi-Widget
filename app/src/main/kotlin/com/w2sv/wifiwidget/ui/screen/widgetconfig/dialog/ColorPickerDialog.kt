package com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colorpicker.model.ColorModel
import com.smarttoolfactory.colorpicker.picker.ColorPickerRingHSL
import com.smarttoolfactory.colorpicker.selector.SelectorRingProperties
import com.w2sv.wifiwidget.ui.designsystem.ColorIndicator
import com.w2sv.wifiwidget.ui.designsystem.ConfigurationDialog
import com.w2sv.wifiwidget.ui.screen.widgetconfig.model.WidgetColor
import com.w2sv.wifiwidget.ui.util.PreviewOf

@Composable
fun ColorPickerDialog(
    data: WidgetConfigDialog.ColorPicker,
    updateColor: (Color) -> Unit,
    onDismissRequest: () -> Unit,
    applyColor: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = Color(data.color)
    ConfigurationDialog(
        onDismissRequest = onDismissRequest,
        onApplyButtonPress = {
            applyColor(data.color)
            onDismissRequest()
        },
        modifier = modifier,
        columnModifier = Modifier.verticalScroll(rememberScrollState()),
        icon = { ColorIndicator(color = color, size = 42.dp) },
        title = stringResource(id = data.widgetColor.labelRes),
        applyButtonEnabled = data.hasBeenConfigured
    ) {
        ColorPickerRingHSL(
            initialColor = color,
            colorModel = ColorModel.RGB,
            ringProperties = SelectorRingProperties(borderStrokeWidth = Dp.Hairline),
            showAlphaSlider = false,
            isColorModelSelectable = false,
            onColorChange = updateColor,
            sliderPanelModifier = Modifier
                .padding(top = 10.dp)
                .padding(horizontal = 10.dp)
        )
    }
}

@Preview
@Composable
private fun Prev() {
    PreviewOf {
        ColorPickerDialog(
            data = WidgetConfigDialog.ColorPicker(
                widgetColor = WidgetColor.Background,
                color = Color.Red.toArgb(),
                initialColor = Color.Red.toArgb()
            ),
            onDismissRequest = {},
            updateColor = {},
            applyColor = {}
        )
    }
}
