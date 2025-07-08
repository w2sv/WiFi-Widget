package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppDefaults {
    val elevatedCardElevation
        @Composable
        get() = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
}

val CardContainerColor: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceContainer

@Composable
fun ElevatedIconHeaderCard(
    iconHeaderProperties: IconHeaderProperties,
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.elevatedCardColors(containerColor = CardContainerColor),
    elevation: CardElevation = AppDefaults.elevatedCardElevation,
    innerPadding: PaddingValues = PaddingValues(18.dp),
    headerPadding: PaddingValues = PaddingValues(bottom = 16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        colors = colors,
        elevation = elevation,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconHeader(
                properties = iconHeaderProperties,
                modifier = Modifier.padding(headerPadding)
            )
            content()
        }
    }
}

@Composable
fun SecondLevelElevatedCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(
        elevation = AppDefaults.elevatedCardElevation,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        modifier = modifier,
        content = content
    )
}
