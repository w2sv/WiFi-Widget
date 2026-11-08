package com.w2sv.widget.ui.util

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.glance.GlanceModifier
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.ExperimentalExtendedContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/** Applies one of two modifier chains to this modifier. */
@OptIn(ExperimentalContracts::class, ExperimentalExtendedContracts::class)
internal inline fun GlanceModifier.thenIf(
    condition: Boolean,
    onFalse: GlanceModifier.() -> GlanceModifier = { this },
    onTrue: GlanceModifier.() -> GlanceModifier = { this }
): GlanceModifier {
    contract {
        callsInPlace(onTrue, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onFalse, InvocationKind.AT_MOST_ONCE)
        condition holdsIn onTrue
        !condition holdsIn onFalse
    }

    return if (condition) onTrue() else onFalse()
}

/** Applies [onTrue] only when [condition] holds. */
@OptIn(ExperimentalContracts::class, ExperimentalExtendedContracts::class)
internal inline fun GlanceModifier.thenIf(condition: Boolean, onTrue: GlanceModifier.() -> GlanceModifier): GlanceModifier {
    contract {
        callsInPlace(onTrue, InvocationKind.AT_MOST_ONCE)
        condition holdsIn onTrue
    }

    return thenIf(condition = condition, onFalse = { this }, onTrue = onTrue)
}

/** Applies [onNotNull] only when [value] is present. */
@OptIn(ExperimentalContracts::class, ExperimentalExtendedContracts::class)
internal inline fun <T> GlanceModifier.thenIfNotNull(value: T?, onNotNull: GlanceModifier.(T) -> GlanceModifier): GlanceModifier {
    contract {
        callsInPlace(onNotNull, InvocationKind.AT_MOST_ONCE)
        (value != null) holdsIn onNotNull
    }

    return value?.let { onNotNull(it) } ?: this
}

@ChecksSdkIntAtLeast(parameter = 0, lambda = 1)
internal inline fun GlanceModifier.thenIfApiAtLeast(api: Int, onTrue: GlanceModifier.() -> GlanceModifier): GlanceModifier =
    if (Build.VERSION.SDK_INT >= api) {
        onTrue()
    } else {
        this
    }
