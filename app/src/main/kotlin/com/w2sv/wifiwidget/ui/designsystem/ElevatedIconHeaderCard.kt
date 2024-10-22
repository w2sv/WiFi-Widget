package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val elevatedCardElevation = 8.dp

@Composable
fun ElevatedIconHeaderCard(
    iconHeaderProperties: IconHeaderProperties,
    modifier: Modifier = Modifier,
    headerRowBottomSpacing: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevatedCardElevation),
        colors = CardDefaults.elevatedCardColors(containerColor = HomeScreenCardBackground),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconHeader(
                properties = iconHeaderProperties,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = headerRowBottomSpacing)
            )
            content()
        }
    }
}
