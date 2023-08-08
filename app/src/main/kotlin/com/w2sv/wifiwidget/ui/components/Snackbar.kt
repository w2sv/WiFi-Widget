package com.w2sv.wifiwidget.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class ExtendedSnackbarVisuals(
    override val message: String,
    val kind: SnackbarKind? = null,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val actionLabel: String? = null,
    val action: (() -> Unit)? = null,
    override val withDismissAction: Boolean = false
) : SnackbarVisuals

enum class SnackbarKind {
    Error,
    Success
}

suspend fun SnackbarHostState.showSnackbarAndDismissCurrentIfApplicable(snackbarVisuals: SnackbarVisuals) {
    currentSnackbarData?.dismiss()
    showSnackbar(snackbarVisuals)
}

@Composable
fun AppSnackbar(snackbarData: SnackbarData) {
    val visuals = snackbarData.visuals as ExtendedSnackbarVisuals

    Snackbar(
        action = {
            visuals.action?.let { action ->
                TextButton(
                    onClick = {
                        action.invoke()
                    }
                ) {
                    JostText(text = visuals.actionLabel!!)
                }
            }
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            (snackbarData.visuals as? ExtendedSnackbarVisuals)?.run {
                when (kind) {
                    SnackbarKind.Error -> Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )

                    SnackbarKind.Success -> Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    null -> Unit
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
            JostText(text = snackbarData.visuals.message)
        }
    }
}