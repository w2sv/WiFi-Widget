package com.w2sv.wifiwidget.ui.screens.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.w2sv.wifiwidget.ui.components.IconHeader
import com.w2sv.wifiwidget.ui.components.IconHeaderProperties

val homeScreenCardElevation = 8.dp

@Composable
fun HomeScreenCard(
    iconHeaderProperties: IconHeaderProperties,
    modifier: Modifier = Modifier,
    headerRowBottomSpacing: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = homeScreenCardElevation),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.background),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 22.dp, horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconHeader(
                properties = iconHeaderProperties,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(headerRowBottomSpacing))
            content()
        }
    }
}
