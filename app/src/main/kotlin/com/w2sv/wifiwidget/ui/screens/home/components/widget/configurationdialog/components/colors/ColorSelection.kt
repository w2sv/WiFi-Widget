package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.colors

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
import com.w2sv.domain.model.WidgetColorSection
import com.w2sv.wifiwidget.R

private const val showColorSelectionDialogRememberKey = "SHOW_COLOR_SELECTION_DIALOG"

@Composable
fun ColorSelection(
    getCustomColor: (WidgetColorSection) -> Color,
    setCustomColor: (WidgetColorSection, Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialogFor by rememberSaveable(key = showColorSelectionDialogRememberKey) {
        mutableStateOf<WidgetColorSection?>(null)
    }
        .apply {
            value?.let { colorSection ->
                ColorPickerDialog(
                    label = stringResource(id = colorSection.labelRes),
                    appliedColor = getCustomColor(colorSection),
                    applyColor = { setCustomColor(colorSection, it) },
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
        WidgetColorSection.entries.forEach {
            SectionCustomizationRow(
                label = stringResource(id = it.labelRes),
                getColor = { getCustomColor(it) },
                onClick = { showDialogFor = it },
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun SectionCustomizationRow(
    label: String,
    getColor: () -> Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.weight(0.2f))
        Text(
            text = bulletPointText(label),
            fontSize = 14.sp,
            modifier = Modifier.weight(0.4f),
        )
        val colorPickerButtonCD =
            stringResource(
                id = R.string.color_picker_button_cd,
                label
            )
        Button(
            modifier = modifier
                .size(36.dp)
                .semantics { contentDescription = colorPickerButtonCD },
            colors = ButtonDefaults.buttonColors(
                containerColor = getColor(),
            ),
            onClick = onClick,
            shape = CircleShape,
            content = {},
        )
        Spacer(modifier = Modifier.weight(0.2f))
    }
}
