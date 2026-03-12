package com.w2sv.wifiwidget.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// TODO: move everything to composed

fun Modifier.resourceIdTestTag(tag: String): Modifier =
    semantics {
        testTagsAsResourceId = true
        testTag = tag
    }

fun Modifier.contentDescription(contentDescription: String): Modifier =
    semantics { this.contentDescription = contentDescription }

/**
 * Allows applying a [Modifier] transformation via a lambda,
 * so you can conditionally or dynamically add modifiers
 * without repeating `Modifier.` in the chain.
 *
 * Example:
 * ```
 * modifier.then {
 *     if (isSelected) background(Color.Green) else background(Brush.horizontalGradient())
 * }
 * ```
 */
@OptIn(ExperimentalContracts::class)
inline fun Modifier.then(block: Modifier.() -> Modifier): Modifier {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return then(block())
}
