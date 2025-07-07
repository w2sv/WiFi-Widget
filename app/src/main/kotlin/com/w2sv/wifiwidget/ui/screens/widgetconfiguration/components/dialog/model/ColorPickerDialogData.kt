package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.CustomWidgetColor

@Stable
class ColorPickerDialogData(val customWidgetColor: CustomWidgetColor, val appliedColor: Color, initialColor: Color) {
    constructor(customWidgetColor: CustomWidgetColor, appliedColor: Color) : this(
        customWidgetColor = customWidgetColor,
        appliedColor = appliedColor,
        initialColor = appliedColor
    )

    var color by mutableStateOf(initialColor)

    val colorsDissimilar by derivedStateOf {
        color != appliedColor
    }

    fun createCustomColoringData(data: WidgetColoring.Style.Custom): WidgetColoring.Style.Custom =
        when (customWidgetColor) {
            CustomWidgetColor.Background -> data.copy(background = color.toArgb())
            CustomWidgetColor.Primary -> data.copy(primary = color.toArgb())
            CustomWidgetColor.Secondary -> data.copy(secondary = color.toArgb())
        }
}
