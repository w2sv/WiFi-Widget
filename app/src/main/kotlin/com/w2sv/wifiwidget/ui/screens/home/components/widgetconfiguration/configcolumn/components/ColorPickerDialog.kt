package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.smarttoolfactory.colorpicker.model.ColorModel
import com.smarttoolfactory.colorpicker.picker.HSVColorPickerCircularWithSliders
import com.smarttoolfactory.colorpicker.widget.ColorComponentsDisplay
import com.w2sv.common.enums.WidgetColor
import com.w2sv.wifiwidget.ui.components.DialogButtonRow
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.theme.AppTheme

@Composable
internal fun ColorPickerDialog(
    widgetSection: WidgetColor,
    appliedColor: Color,
    onDismissRequest: () -> Unit,
    applyColor: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    var color by remember {
        mutableStateOf(
            appliedColor
        )
    }

    Dialog(onDismissRequest = onDismissRequest) {
        ElevatedCard(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.elevatedCardElevation(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                JostText(
                    text = stringResource(id = widgetSection.labelRes),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                HSVColorPickerCircularWithSliders(
                    initialColor = color,
                    onColorChange = { newColor, _ -> color = newColor }
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
                        .padding(vertical = 16.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun Prev() {
    AppTheme {
        ColorPickerDialog(
            widgetSection = WidgetColor.Background,
            appliedColor = Color.Red,
            applyColor = {},
            onDismissRequest = {}
        )
    }
}