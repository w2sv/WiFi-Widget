package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.w2sv.common.enums.WidgetColorSection
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.DialogButton
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.theme.AppTheme

@Composable
internal fun ColorPickerDialog(
    widgetSection: WidgetColorSection,
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
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismissRequest) {
        ElevatedCard(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.elevatedCardElevation(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                JostText(
                    text = stringResource(id = widgetSection.labelRes),
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize
                )
                Spacer(modifier = Modifier.padding(vertical = 6.dp))
                HSVColorPickerCircularWithSliders(
                    initialColor = color,
                    onColorChange = { newColor, _ -> color = newColor }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    ColorComponentsDisplay(
                        color = color,
                        colorModel = ColorModel.RGB,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(220.dp),
                    )
                }
                Spacer(modifier = Modifier.padding(vertical = 12.dp))
                ButtonRow(
                    onCancelButtonPress = onDismissRequest,
                    onApplyButtonPress = {
                        applyColor(color)
                        onDismissRequest()
                    }
                )
            }
        }
    }
}

@Composable
private fun ButtonRow(
    onCancelButtonPress: () -> Unit,
    onApplyButtonPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        DialogButton(onClick = onCancelButtonPress) {
            JostText(text = stringResource(id = R.string.cancel))
        }
        Spacer(modifier = Modifier.padding(horizontal = 12.dp))
        DialogButton(
            onClick = onApplyButtonPress
        ) {
            JostText(text = stringResource(id = R.string.apply))
        }
    }
}

@Preview
@Composable
private fun Prev() {
    AppTheme {
        ColorPickerDialog(
            widgetSection = WidgetColorSection.Background,
            appliedColor = Color.Red,
            applyColor = {},
            onDismissRequest = {}
        )
    }
}