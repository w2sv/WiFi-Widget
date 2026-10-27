package com.w2sv.wifiwidget.ui.util.snackbar

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind

val LocalSnackbarVisibility = staticCompositionLocalOf { SnackbarVisibility() }

/**
 * Tracks actual snackbar visibility and kind through its exit animation. [set] belongs to the snackbar host.
 */
@Stable
class SnackbarVisibility {
    private var state by mutableStateOf(State())

    val isVisible: Boolean
        get() = state.isVisible

    val kind: SnackbarKind?
        get() = state.kind

    internal fun setVisible(kind: SnackbarKind? = null) {
        state = State(
            isVisible = true,
            kind = kind
        )
    }

    internal fun setHidden() {
        state = State()
    }

    private data class State(val isVisible: Boolean = false, val kind: SnackbarKind? = null)
}
