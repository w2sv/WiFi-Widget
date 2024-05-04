package com.w2sv.wifiwidget.ui.designsystem

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@SuppressLint("ComposeComposableModifier", "ComposeModifierWithoutDefault")
@Composable
@ReadOnlyComposable
fun Modifier.nestedContentBackground(
    color: Color = MaterialTheme.colorScheme.surface,
    shape: Shape = MaterialTheme.shapes.medium
): Modifier =
    this then Modifier
        .background(
            color = color,
            shape = shape
        )

val HomeScreenCardBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceColorAtElevation(
        elevatedCardElevation
    )