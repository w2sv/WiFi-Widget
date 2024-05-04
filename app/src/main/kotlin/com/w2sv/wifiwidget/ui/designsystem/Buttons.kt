package com.w2sv.wifiwidget.ui.designsystem

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DialogButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.elevatedButtonColors(
        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.primary,
    ),
    content: @Composable (RowScope.() -> Unit),
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        border = if (enabled) {
            BorderStroke(
                Dp.Hairline,
                MaterialTheme.colorScheme.primary,
            )
        } else {
            null
        },
        elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
        colors = colors,
        content = content,
    )
}

@SuppressLint("ComposeComposableModifier")
@ReadOnlyComposable
@Composable
fun Modifier.colorButton(): Modifier =
    this then Modifier
        .border(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = CircleShape
        )
        .size(42.dp)