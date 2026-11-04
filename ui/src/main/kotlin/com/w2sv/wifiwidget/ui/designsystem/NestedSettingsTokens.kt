package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object NestedSettingsTokens {
    val containerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surfaceContainerHigh

    val shape: Shape
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes.medium

    val startPadding = 10.dp
    val bottomMargin = 4.dp
    val topMargin = 8.dp
    val secondLevelStartPadding = 24.dp

    // TLayout anchors expanded content at the row label; this offset aligns the tonal container around that label.
    val containerStartOffset = (-8).dp
    val fontSize = 14.sp
}
