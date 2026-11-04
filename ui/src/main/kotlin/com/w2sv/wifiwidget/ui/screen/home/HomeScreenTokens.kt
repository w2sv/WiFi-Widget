package com.w2sv.wifiwidget.ui.screen.home

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object HomeScreenTokens {
    val screenHorizontalPadding = 16.dp
    val screenTopPadding = 8.dp
    val scrollContentBottomPadding: Dp
        @Composable
        get() = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding() + 16.dp

    val buttonHeight = 56.dp
}
