package com.w2sv.wifiwidget.ui.util.snackbar

import android.content.Context
import androidx.compose.material3.SnackbarVisuals
import kotlinx.coroutines.flow.Flow

typealias SnackbarBuilder = Context.() -> SnackbarVisuals
typealias SnackbarBuilderFlow = Flow<SnackbarBuilder>
