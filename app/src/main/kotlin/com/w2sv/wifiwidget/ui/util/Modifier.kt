package com.w2sv.wifiwidget.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId

// TODO: composed
fun Modifier.resourceIdTestTag(tag: String): Modifier =
    semantics {
        testTagsAsResourceId = true
        testTag = tag
    }

fun Modifier.contentDescription(contentDescription: String): Modifier =
    semantics { this.contentDescription = contentDescription }
