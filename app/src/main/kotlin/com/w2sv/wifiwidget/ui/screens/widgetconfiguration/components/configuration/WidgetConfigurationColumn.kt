package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w2sv.composed.isPortraitModeActive
import com.w2sv.domain.model.WidgetBottomBarElement
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.ElevatedIconHeaderCard
import com.w2sv.wifiwidget.ui.designsystem.IconHeaderProperties
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration.appearance.appearanceConfigurationCard
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.ColorPickerDialogData
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.InfoDialogData
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.ReversibleWidgetConfiguration
import com.w2sv.wifiwidget.ui.states.LocationAccessState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

private val verticalCardSpacing = 16.dp
private val checkRowColumnBottomPadding = 8.dp
private val innerWidgetConfigurationCardPadding = PaddingValues(vertical = 18.dp)

@Composable
fun WidgetConfigurationColumn(cardProperties: ImmutableList<WidgetConfigurationCard>, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = if (isPortraitModeActive) 26.dp else 126.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(verticalCardSpacing),
            modifier = Modifier.padding(
                top = verticalCardSpacing,
                bottom = if (isPortraitModeActive) 142.dp else 92.dp
            )
        ) {
            cardProperties.forEach { section ->
                WidgetConfigurationCard(properties = section)
            }
        }
    }
}

@Composable
private fun WidgetConfigurationCard(properties: WidgetConfigurationCard, modifier: Modifier = Modifier) {
    ElevatedIconHeaderCard(
        iconHeaderProperties = properties.iconHeaderProperties,
        innerPadding = innerWidgetConfigurationCardPadding,
        modifier = modifier
    ) {
        properties.content()
    }
}

@Immutable
data class WidgetConfigurationCard(val iconHeaderProperties: IconHeaderProperties, val content: @Composable () -> Unit)

@Composable
fun rememberWidgetConfigurationCards(
    widgetConfiguration: ReversibleWidgetConfiguration,
    locationAccessState: LocationAccessState,
    showInfoDialog: (InfoDialogData) -> Unit,
    showCustomColorConfigurationDialog: (ColorPickerDialogData) -> Unit,
    showRefreshIntervalConfigurationDialog: () -> Unit
): ImmutableList<WidgetConfigurationCard> =
    remember {
        persistentListOf(
            appearanceConfigurationCard(
                widgetConfiguration = widgetConfiguration,
                showCustomColorConfigurationDialog = showCustomColorConfigurationDialog
            ),
            propertiesConfigurationCard(
                widgetConfiguration = widgetConfiguration,
                locationAccessState = locationAccessState,
                showInfoDialog = showInfoDialog
            ),
            bottomBarElementsConfigurationCard(
                widgetConfiguration = widgetConfiguration,
                contentModifier = Modifier.padding(bottom = checkRowColumnBottomPadding)
            ),
            refreshingConfigurationCard(
                widgetConfiguration = widgetConfiguration,
                showInfoDialog = showInfoDialog,
                showRefreshIntervalConfigurationDialog = showRefreshIntervalConfigurationDialog,
                contentModifier = Modifier.padding(bottom = checkRowColumnBottomPadding)
            )
        )
    }

private fun bottomBarElementsConfigurationCard(
    widgetConfiguration: ReversibleWidgetConfiguration,
    contentModifier: Modifier = Modifier
): WidgetConfigurationCard =
    WidgetConfigurationCard(
        iconHeaderProperties = IconHeaderProperties(
            iconRes = R.drawable.ic_bottom_row_24,
            stringRes = R.string.bottom_bar
        )
    ) {
        CheckRowColumn(
            elements = remember {
                WidgetBottomBarElement.entries.map {
                    ConfigurationColumnElement.CheckRow.fromIsCheckedMap(
                        property = it,
                        explanation = it.explanation,
                        isCheckedMap = widgetConfiguration.bottomRowMap
                    )
                }
                    .toPersistentList()
            },
            modifier = contentModifier
        )
    }
