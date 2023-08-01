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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.w2sv.data.model.WidgetColor
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.bulletPointText
import com.w2sv.wifiwidget.ui.utils.toColor

@Stable
private data class WidgetColorSectionData(
    val widgetColor: WidgetColor,
    val color: Color,
    val label: String
)

@Composable
internal fun ColorSelection(
    widgetColors: MutableMap<WidgetColor, Int>,
    modifier: Modifier = Modifier
) {
    val sectionData by remember {
        derivedStateOf {
            listOf(
                WidgetColorSectionData(
                    WidgetColor.Background,
                    widgetColors.getValue(WidgetColor.Background).toColor(),
                    "Background"
                ),
                WidgetColorSectionData(
                    WidgetColor.Primary,
                    widgetColors.getValue(WidgetColor.Primary).toColor(),
                    "Primary"
                ),
                WidgetColorSectionData(
                    WidgetColor.Secondary,
                    widgetColors.getValue(WidgetColor.Secondary).toColor(),
                    "Secondary"
                )
            )
        }
    }

    var showDialogFor by rememberSaveable {
        mutableStateOf<WidgetColorSectionData?>(null)
    }
        .apply {
            value?.let {
                ColorPickerDialog(
                    label = it.label,
                    appliedColor = it.color,
                    applyColor = { color ->
                        widgetColors[it.widgetColor] = color.toArgb()
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
        sectionData.forEach {
            SectionCustomizationRow(
                sectionData = it,
                onClick = { showDialogFor = it },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SectionCustomizationRow(
    sectionData: WidgetColorSectionData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorPickerButtonCD =
        stringResource(id = R.string.color_picker_button_cd).format(sectionData.label)

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(0.2f))
        JostText(
            text = bulletPointText(sectionData.label),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.4f)
        )
        Button(
            modifier = modifier
                .size(36.dp)
                .semantics { contentDescription = colorPickerButtonCD },
            colors = ButtonDefaults.buttonColors(
                containerColor = sectionData.color
            ),
            onClick = onClick,
            shape = CircleShape,
            content = {}
        )
        Spacer(modifier = Modifier.weight(0.2f))
    }
}