package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.configcolumn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
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
        SectionCustomizationRow(stringResource(R.string.labels), CustomizableSection.Labels)
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
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        JostText(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.margin_minimal))
        )
        Spacer(modifier = Modifier.padding(horizontal = 16.dp))
        Button(
            modifier = modifier,
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
    }
}