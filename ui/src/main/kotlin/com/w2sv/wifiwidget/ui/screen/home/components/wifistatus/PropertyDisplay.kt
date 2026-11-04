package com.w2sv.wifiwidget.ui.screen.home.components.wifistatus

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentation

@Composable
internal fun PropertyDisplay(
    presentation: WifiPropertyPresentation,
    label: AnnotatedString,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(0.4f)
        )
        AnimatedContent(targetState = presentation, modifier = Modifier.weight(1f)) { content ->
            Column {
                Text(
                    text = content.value,
                    color = if (content.resolutionError == null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                if (content.subValues.isNotEmpty()) {
                    Text(
                        text = content.subValues.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
