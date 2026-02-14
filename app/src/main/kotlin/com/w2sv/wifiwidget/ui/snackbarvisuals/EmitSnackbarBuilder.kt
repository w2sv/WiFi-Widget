package com.w2sv.wifiwidget.ui.snackbarvisuals

import android.content.Context
import androidx.compose.material3.SnackbarVisuals

typealias SnackbarBuilder = Context.() -> SnackbarVisuals

fun interface EmitSnackbarBuilder {
    suspend operator fun invoke(value: SnackbarBuilder)
}
