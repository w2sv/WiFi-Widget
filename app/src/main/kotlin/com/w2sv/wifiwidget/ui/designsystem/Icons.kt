package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object IconDefaults {
    val SizeBig = 28.dp
}

@Composable
fun InfoIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = LocalContentColor.current
) {
    Icon(
        imageVector = Icons.Outlined.Info,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
    )
}

@Composable
fun KeyboardArrowRightIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        modifier = modifier,
        tint = tint
    )
}

@Composable
fun SubPropertyKeyboardArrowRightIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    KeyboardArrowRightIcon(
        modifier = modifier.size(20.dp),
        tint = tint
    )
}
