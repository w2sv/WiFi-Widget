package com.w2sv.widget.ui.util

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable

/**
 * Glance replacement for androidx.compose.ui.res.stringResource().
 *
 * Usage inside your Glance `Content()`:
 *
 *     Text(text = stringResource(R.string.hello_world))
 *
 * Supports positional formatting just like Context#getString().
 */
@Composable
internal fun stringResource(@StringRes id: Int, vararg formatArgs: Any): String {
    val context = androidx.glance.LocalContext.current
    return context.getString(id, *formatArgs)
}

@Composable
internal fun quantityStringResource(
    @PluralsRes id: Int,
    quantity: Int,
    vararg formatArgs: Any
): String {
    val context = androidx.glance.LocalContext.current
    return context.resources.getQuantityString(id, quantity, *formatArgs)
}
