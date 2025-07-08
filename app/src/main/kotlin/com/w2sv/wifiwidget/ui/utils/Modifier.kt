package com.w2sv.wifiwidget.ui.utils

import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId

@ReadOnlyComposable
fun Modifier.resourceIdTestTag(tag: String): Modifier =
    semantics {
        testTagsAsResourceId = true
        testTag = tag
    }
