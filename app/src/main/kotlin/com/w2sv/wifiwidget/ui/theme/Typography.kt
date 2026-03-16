package com.w2sv.wifiwidget.ui.theme

import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.sp

val Typography.explanation
    @ReadOnlyComposable
    @Composable
    get() = bodyMedium.copy(fontSize = 13.sp, color = colorScheme.onSurfaceVariantLowAlpha)
