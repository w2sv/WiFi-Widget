package com.w2sv.wifiwidget.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

fun Modifier.contentDescription(contentDescription: String): Modifier =
    semantics { this.contentDescription = contentDescription }
