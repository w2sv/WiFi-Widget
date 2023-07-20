package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.enums.WidgetColor
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.bulletPointText
import com.w2sv.wifiwidget.ui.utils.toColor

@Composable
internal fun ColorSelection(
    widgetColors: MutableMap<WidgetColor, Int>,
    modifier: Modifier = Modifier
) {
    var showDialogFor by rememberSaveable {
        mutableStateOf<WidgetColor?>(null)
    }
        .apply {
            value?.let {
                ColorPickerDialog(
                    widgetSection = it,
                    appliedColor = widgetColors.getValue(it).toColor(),
                    applyColor = { color ->
                        widgetColors[it] = color.toArgb()
                    },
                    onDismissRequest = {
                        value = null
                    }
                )
            }
        }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        WidgetColor.values().forEach {
            SectionCustomizationRow(
                widgetColor = it,
                color = widgetColors.getValue(it).toColor(),
                onClick = { showDialogFor = it },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SectionCustomizationRow(
    widgetColor: WidgetColor,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = stringResource(id = widgetColor.labelRes)
    val colorPickerButtonCD = stringResource(id = R.string.color_picker_button_cd).format(label)

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(0.2f))
        JostText(
            text = bulletPointText(label),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.4f)
        )
        Button(
            modifier = modifier
                .size(36.dp)
                .semantics { contentDescription = colorPickerButtonCD },
            colors = ButtonDefaults.buttonColors(
                containerColor = color
            ),
            onClick = onClick,
            shape = CircleShape,
            content = {}
        )
        Spacer(modifier = Modifier.weight(0.2f))
    }
}