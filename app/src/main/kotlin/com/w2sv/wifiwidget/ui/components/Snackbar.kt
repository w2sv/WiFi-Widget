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
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

data class SnackbarAction(val label: String, val callback: () -> Unit)

data class AppSnackbarVisuals(
    val msg: CharSequence,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val action: SnackbarAction? = null,
    val kind: SnackbarKind? = null,
    override val withDismissAction: Boolean = false,
) : SnackbarVisuals {

    override val message: String
        get() = msg as String

    override val actionLabel: String?
        get() = action?.label
}

sealed interface SnackbarKind {
    val icon: ImageVector

    @get:Composable
    val iconTint: Color

    data object Error : SnackbarKind {
        override val icon: ImageVector = Icons.Outlined.Warning
        override val iconTint: Color
            @Composable get() = MaterialTheme.colorScheme.error
    }

    data object Success : SnackbarKind {
        override val icon: ImageVector = Icons.Outlined.Check
        override val iconTint: Color
            @Composable get() = MaterialTheme.colorScheme.primary
    }
}

val LocalSnackbarHostState = compositionLocalOf { SnackbarHostState() }

suspend fun SnackbarHostState.showSnackbarAndDismissCurrentIfApplicable(snackbarVisuals: SnackbarVisuals) {
    currentSnackbarData?.dismiss()
    showSnackbar(snackbarVisuals)
}

@Composable
fun AppSnackbar(visuals: AppSnackbarVisuals) {
    Snackbar(
        action = {
            visuals.action?.let { action ->
                TextButton(
                    onClick = action.callback,
                ) {
                    AppFontText(
                        text = action.label,
                        color = SnackbarDefaults.actionColor
                    )
                }
            }
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            visuals.kind?.let { kind ->
                Icon(imageVector = kind.icon, contentDescription = null, tint = kind.iconTint)
                Spacer(modifier = Modifier.width(10.dp))
            }
            if (visuals.msg is AnnotatedString) {
                AppFontText(text = visuals.msg)
            } else {
                AppFontText(text = visuals.message)
            }
        }
    }
}
