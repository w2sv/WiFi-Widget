package com.w2sv.wifiwidget.ui.util.snackbar

import androidx.compose.material3.SnackbarHostState

/**
 * Shortcut for `currentSnackbarData?.dismiss()`.
 */
fun SnackbarHostState.dismissCurrentSnackbar() {
    currentSnackbarData?.dismiss()
}
