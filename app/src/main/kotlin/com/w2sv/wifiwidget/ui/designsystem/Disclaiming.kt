package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.wifiwidget.ui.theme.onSurfaceVariantLowAlpha
import com.w2sv.wifiwidget.ui.util.WithLocalContentColor

@Composable
fun DisclaimerRow(text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WithLocalContentColor(MaterialTheme.colorScheme.onSurfaceVariantLowAlpha) {
            InfoIcon()
            Text(text = text, fontSize = 13.sp)
        }
    }
}
