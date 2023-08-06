package com.w2sv.wifiwidget.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun disabledColor(): Color =
    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)