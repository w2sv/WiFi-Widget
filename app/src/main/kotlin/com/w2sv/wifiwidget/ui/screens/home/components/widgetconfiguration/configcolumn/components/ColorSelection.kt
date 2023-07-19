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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.common.enums.WidgetColorSection
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.bulletPointText
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.WidgetConfigurationViewModel

@Composable
internal fun ColorSelection(
    modifier: Modifier = Modifier,
    widgetConfigurationVM: WidgetConfigurationViewModel = viewModel()
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        WidgetColorSection.values().forEach {
            SectionCustomizationRow(
                widgetColorSection = it,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }

    widgetConfigurationVM.customizationDialogSection.collectAsState().value?.let { section ->
        ColorPickerDialog(section)
    }
}

@Composable
private fun SectionCustomizationRow(
    widgetColorSection: WidgetColorSection,
    modifier: Modifier = Modifier,
    widgetConfigurationVM: WidgetConfigurationViewModel = viewModel()
) {
    val label = stringResource(id = widgetColorSection.labelRes)
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
                containerColor = Color(
                    widgetConfigurationVM.nonAppliedWidgetColors.getValue(
                        widgetColorSection
                    )
                )
            ),
            onClick = {
                widgetConfigurationVM.customizationDialogSection.value = widgetColorSection
            },
            shape = CircleShape
        ) {}
        Spacer(modifier = Modifier.weight(0.2f))
    }
}