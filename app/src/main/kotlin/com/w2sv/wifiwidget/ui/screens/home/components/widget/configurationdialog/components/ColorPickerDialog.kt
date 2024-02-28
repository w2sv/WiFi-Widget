package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colorpicker.model.ColorModel
import com.smarttoolfactory.colorpicker.picker.HSVColorPickerCircularWithSliders
import com.smarttoolfactory.colorpicker.widget.ColorComponentsDisplay
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.wifiwidget.ui.designsystem.ConfigurationDialog
import com.w2sv.wifiwidget.ui.utils.nullableListSaver

@Stable
class ColorPickerProperties(
    val widgetColorType: WidgetColorType,
    private val appliedColor: Color,
    initialColor: Color
) {
    constructor(widgetColorType: WidgetColorType, appliedColor: Color) : this(
        widgetColorType = widgetColorType,
        appliedColor = appliedColor,
        initialColor = appliedColor
    )

    var color by mutableStateOf(initialColor)

    val colorsDissimilar by derivedStateOf {
        color != appliedColor
    }

    fun createCustomColoringData(data: WidgetColoring.Data.Custom): WidgetColoring.Data.Custom =
        when (widgetColorType) {
            WidgetColorType.Background -> data.copy(background = color.toArgb())
            WidgetColorType.Primary -> data.copy(primary = color.toArgb())
            WidgetColorType.Secondary -> data.copy(secondary = color.toArgb())
        }

    companion object {
        val nullableStateSaver = nullableListSaver<ColorPickerProperties, Any>(
            saveNonNull = {
                listOf(it.widgetColorType, it.appliedColor.toArgb(), it.color.toArgb())
            },
            restoreNonNull = {
                ColorPickerProperties(
                    widgetColorType = it[0] as WidgetColorType,
                    appliedColor = Color(it[1] as Int),
                    initialColor = Color(it[2] as Int)
                )
            }
        )
    }
}

@Composable
fun ColorPickerDialog(
    properties: ColorPickerProperties,
    onDismissRequest: () -> Unit,
    applyColor: (Color) -> Unit,
    modifier: Modifier = Modifier,
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
        title = stringResource(id = properties.widgetColorType.labelRes),
        applyButtonEnabled = properties.colorsDissimilar
    ) {
        HSVColorPickerCircularWithSliders(
            initialColor = properties.color,
            onColorChange = remember { { newColor, _ -> properties.color = newColor } },
        )
        ColorComponentsDisplay(
            color = properties.color,
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
