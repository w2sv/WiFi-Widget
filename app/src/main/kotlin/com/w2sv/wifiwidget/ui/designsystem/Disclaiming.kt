package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w2sv.wifiwidget.ui.theme.explanation
import com.w2sv.wifiwidget.ui.theme.onSurfaceVariantLowAlpha
import com.w2sv.wifiwidget.ui.util.WithLocalContentColor

@Composable
fun Disclaimer(text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WithLocalContentColor(colorScheme.onSurfaceVariantLowAlpha) {
            InfoIcon()
            ExplanationText(text)
        }
    }
}

@Composable
fun ExplanationText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = typography.explanation,
        modifier = modifier
    )
}
