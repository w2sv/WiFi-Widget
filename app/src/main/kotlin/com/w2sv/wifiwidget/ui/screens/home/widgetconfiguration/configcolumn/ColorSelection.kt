package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.configcolumn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.HomeActivity
import com.w2sv.wifiwidget.ui.shared.JostText

enum class CustomizableSection {
    Background,
    Labels,
    Other
}

@Composable
internal fun ColorSelectionRow(
    modifier: Modifier = Modifier,
    viewModel: HomeActivity.ViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        SectionCustomizationRow(stringResource(R.string.background), CustomizableSection.Background)
        Spacer(modifier = Modifier.padding(vertical = 4.dp))
        SectionCustomizationRow(stringResource(R.string.labels), CustomizableSection.Labels)
        Spacer(modifier = Modifier.padding(vertical = 4.dp))
        SectionCustomizationRow(stringResource(R.string.other), CustomizableSection.Other)
    }

    viewModel.customizationDialogSection.collectAsState().value?.let { section ->
        ColorPickerDialog(
            properties = when (section) {
                CustomizableSection.Background -> Properties(stringResource(id = R.string.background))

                CustomizableSection.Labels -> Properties(stringResource(id = R.string.labels))

                CustomizableSection.Other -> Properties(stringResource(id = R.string.other))
            }
        )
    }
}

@Composable
private fun SectionCustomizationRow(
    label: String,
    customizableSection: CustomizableSection,
    modifier: Modifier = Modifier,
    viewModel: HomeActivity.ViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(0.2f))
        JostText(
            text = label,
            fontSize = 12.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.3f)
        )
        Spacer(modifier = Modifier.weight(0.1f))
        Button(
            modifier = modifier.size(36.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(
                    viewModel.customWidgetColorsState.getValue(
                        label
                    )
                )
            ),
            onClick = {
                viewModel.customizationDialogSection.value = customizableSection
            },
            shape = CircleShape
        ) {}
        Spacer(modifier = Modifier.weight(0.4f))
    }
}