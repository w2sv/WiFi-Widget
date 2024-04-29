package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val biggerIconSize = 28.dp

@Composable
fun InfoIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Icon(
        imageVector = Icons.Outlined.Info,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier,
    )
}
