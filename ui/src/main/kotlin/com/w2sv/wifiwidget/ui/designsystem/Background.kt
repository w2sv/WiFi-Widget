package com.w2sv.wifiwidget.ui.designsystem

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@SuppressLint("ComposeComposableModifier", "ComposeModifierWithoutDefault")
@Composable
@ReadOnlyComposable
fun Modifier.nestedContentBackground(
    color: Color = NestedSettingsTokens.containerColor,
    shape: Shape = NestedSettingsTokens.shape
): Modifier =
    this then Modifier
        .background(
            color = color,
            shape = shape
        )
