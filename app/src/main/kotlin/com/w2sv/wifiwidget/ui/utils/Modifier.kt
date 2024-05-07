package com.w2sv.wifiwidget.ui.utils

import androidx.compose.ui.Modifier

// TODO: export to composed
inline fun <T> Modifier.thenIfNotNull(
    instance: T?,
    onNotNull: Modifier.(T) -> Modifier,
): Modifier =
    instance?.let { onNotNull(it) } ?: this