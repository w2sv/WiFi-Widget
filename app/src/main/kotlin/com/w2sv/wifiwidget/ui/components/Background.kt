package com.w2sv.wifiwidget.ui.components

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Modifier.nestedListBackground(): Modifier =
    this then Modifier
        .background(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium
        )