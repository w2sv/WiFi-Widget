package com.w2sv.wifiwidget.ui.screen.locationaccess

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

internal object LocationAccessScreenTokens {
    val screenHorizontalPadding = 24.dp
    val landscapeColumnSpacing = 24.dp
    val screenContentPadding = PaddingValues(
        start = screenHorizontalPadding,
        end = screenHorizontalPadding,
        top = 14.dp,
        bottom = 24.dp
    )
    val explanationIconSize = 24.dp

    val actionPanelContentPadding = PaddingValues(
        start = screenHorizontalPadding,
        top = 16.dp,
        end = screenHorizontalPadding,
        bottom = 12.dp
    )
    val actionSpacing = 8.dp
    val actionButtonMinHeight = 56.dp

    val statusBottomPadding = 8.dp
    val statusMinHeight = 48.dp
    val statusContentSpacing = 12.dp
    val statusIconSize = 20.dp
    val statusChevronSize = 24.dp

    val disclosureTextSpacing = 12.dp
}
