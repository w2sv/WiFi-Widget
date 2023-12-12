package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components.colors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.utils.bulletPointText
import com.w2sv.domain.model.WidgetColor
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.utils.toColor
import kotlinx.collections.immutable.ImmutableMap

private data class WidgetColorSectionData(
    val widgetColor: WidgetColor,
    val getColor: () -> Color,
)

@Composable
fun ColorSelection(
    customColors: ImmutableMap<WidgetColor, Int>,
    setCustomColor: (WidgetColor, Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sectionData = remember {
        WidgetColor.entries.map {
            WidgetColorSectionData(
                widgetColor = it,
                getColor = { customColors.getValue(it).toColor() },
            )
        }
    }

    var showDialogFor by rememberSaveable {
        mutableStateOf<WidgetColorSectionData?>(null)
    }
        .apply {
            value?.let { color ->
                ColorPickerDialog(
                    label = stringResource(id = color.widgetColor.labelRes),
                    appliedColor = color.getColor(),
                    applyColor = { setCustomColor(color.widgetColor, it) },
                    onDismissRequest = {
                        value = null
                    },
                )
            }
        }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier,
    ) {
        sectionData.forEach {
            SectionCustomizationRow(
                sectionData = it,
                onClick = { showDialogFor = it },
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun SectionCustomizationRow(
    sectionData: WidgetColorSectionData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorPickerButtonCD =
        stringResource(
            id = R.string.color_picker_button_cd,
            stringResource(id = sectionData.widgetColor.labelRes)
        )

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.weight(0.2f))
        Text(
            text = bulletPointText(stringResource(id = sectionData.widgetColor.labelRes)),
            fontSize = 14.sp,
            modifier = Modifier.weight(0.4f),
        )
        Button(
            modifier = modifier
                .size(36.dp)
                .semantics { contentDescription = colorPickerButtonCD },
            colors = ButtonDefaults.buttonColors(
                containerColor = sectionData.getColor(),
            ),
            onClick = onClick,
            shape = CircleShape,
            content = {},
        )
        Spacer(modifier = Modifier.weight(0.2f))
    }
}
