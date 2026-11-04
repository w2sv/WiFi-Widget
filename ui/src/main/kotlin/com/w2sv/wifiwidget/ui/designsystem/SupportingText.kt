package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.w2sv.composed.material3.WithLocalContentColor
import com.w2sv.wifiwidget.ui.designsystem.theme.explanation
import com.w2sv.wifiwidget.ui.designsystem.theme.onSurfaceVariantLowAlpha

@Composable
fun SupportingTextWithInfoIcon(
    text: String,
    modifier: Modifier = Modifier,
    iconTextSpacing: Dp = ButtonDefaults.IconSpacing
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(iconTextSpacing)
    ) {
        WithLocalContentColor(colorScheme.onSurfaceVariantLowAlpha) {
            InfoIcon()
            SupportingText(text)
        }
    }
}

@Composable
fun SupportingText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = typography.explanation,
        modifier = modifier
    )
}
