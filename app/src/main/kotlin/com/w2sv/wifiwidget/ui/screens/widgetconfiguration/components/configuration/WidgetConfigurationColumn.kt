package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w2sv.composed.isPortraitModeActive
import com.w2sv.wifiwidget.ui.designsystem.ElevatedIconHeaderCard
import kotlinx.collections.immutable.ImmutableList

private val verticalColumnCardSpacing = 16.dp

@Composable
fun WidgetConfigurationColumn(
    cardProperties: ImmutableList<WidgetConfigurationCard>,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = if (isPortraitModeActive) 26.dp else 126.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(verticalColumnCardSpacing),
            modifier = Modifier.padding(
                top = verticalColumnCardSpacing,
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
        innerPadding = PaddingValues(vertical = 18.dp),
        modifier = modifier
    ) {
        properties.content()
    }
}
