package com.w2sv.wifiwidget.ui.designsystem.configlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Stable
class ExpandableListState(initialExpanded: Boolean, val allowCollapsing: Boolean) {
    var isExpanded by mutableStateOf(initialExpanded)
        private set

    /** Toggle the expansion manually */
    fun toggle() {
        if (allowCollapsing) {
            isExpanded = !isExpanded
        }
    }

    /** Forcefully set expansion */
    fun expand(expand: Boolean) {
        isExpanded = expand
    }
}

@Composable
fun rememberExpandableListState(isPropertyEnabled: Boolean, allowCollapsing: Boolean): ExpandableListState {
    val state = rememberSaveable(
        saver = Saver(
            save = { it.isExpanded },
            restore = { ExpandableListState(it, allowCollapsing) }
        )
    ) {
        ExpandableListState(
            initialExpanded = !allowCollapsing && isPropertyEnabled,
            allowCollapsing = allowCollapsing
        )
    }

    // Update automatically when data changes
    LaunchedEffect(isPropertyEnabled) {
        when {
            !isPropertyEnabled -> state.expand(false)
            !allowCollapsing -> state.expand(true)
        }
    }

    return state
}
