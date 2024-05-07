package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration_column

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w2sv.composed.isPortraitModeActive
import com.w2sv.wifiwidget.ui.designsystem.HomeScreenCardBackground
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.ColorPickerDialogData
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.InfoDialogData
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.ReversibleWidgetConfiguration
import com.w2sv.wifiwidget.ui.states.LocationAccessState
import kotlinx.collections.immutable.ImmutableList

@Composable
fun WidgetPropertyConfigurationColumn(
    widgetConfiguration: ReversibleWidgetConfiguration,
    locationAccessState: LocationAccessState,
    showPropertyInfoDialog: (InfoDialogData) -> Unit,
    showCustomColorConfigurationDialog: (ColorPickerDialogData) -> Unit,
    showRefreshIntervalConfigurationDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = if (isPortraitModeActive) 26.dp else 126.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        SectionCardColumn(
            sectionCardProperties = rememberSectionCardProperties(
                widgetConfiguration = widgetConfiguration,
                locationAccessState = locationAccessState,
                showInfoDialog = showPropertyInfoDialog,
                showCustomColorConfigurationDialog = showCustomColorConfigurationDialog,
                showRefreshIntervalConfigurationDialog = showRefreshIntervalConfigurationDialog
            )
        )
        Spacer(modifier = Modifier.height(if (isPortraitModeActive) 142.dp else 92.dp))
    }
}

@Composable
private fun SectionCardColumn(
    sectionCardProperties: ImmutableList<SectionCardProperties>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        sectionCardProperties
            .forEach { section ->
                SectionCard(properties = section)
            }
    }
}

@Composable
private fun SectionCard(properties: SectionCardProperties, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(
                color = HomeScreenCardBackground,
                shape = MaterialTheme.shapes.medium
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        IconHeader(
            properties = properties.iconHeaderProperties,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
        )
        properties.content()
    }
}