package com.w2sv.wifiwidget.ui.designsystem

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// defaultElevation = 1.dp
private val cardElevation
    @Composable
    get() = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)

@Composable
fun ElevatedCardWithHeader(
    @DrawableRes iconRes: Int,
    @StringRes stringRes: Int,
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.elevatedCardColors(),
    elevation: CardElevation = cardElevation,
    innerPadding: PaddingValues = PaddingValues(18.dp),
    headerPadding: PaddingValues = PaddingValues(bottom = IconHeadlineTokens.bottomSpacing),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        IconHeadline(
            iconRes = iconRes,
            stringRes = stringRes,
            modifier = Modifier.padding(headerPadding)
        )

        ElevatedCard(
            colors = colors,
            elevation = elevation
        ) {
            Column(
                modifier = Modifier.padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content
            )
        }
    }
}
