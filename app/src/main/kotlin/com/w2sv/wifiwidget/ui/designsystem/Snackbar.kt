package com.w2sv.wifiwidget.ui.designsystem

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.CollectLatestFromFlow
import com.w2sv.composed.core.OnDispose
import com.w2sv.wifiwidget.ui.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.LocalSnackbarVisibility
import com.w2sv.wifiwidget.ui.theme.AppColor
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarBuilderFlow
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarController
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarVisibility
import com.w2sv.wifiwidget.ui.util.snackbar.rememberSnackbarController
import kotlinx.coroutines.flow.emptyFlow

@Immutable
data class SnackbarAction(val label: String, val callback: () -> Unit)

@Immutable
data class AppSnackbarVisuals(
    val msg: CharSequence,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val action: SnackbarAction? = null,
    val kind: SnackbarKind? = null,
    override val withDismissAction: Boolean = false
) : SnackbarVisuals {

    override val message: String
        get() = msg as String

    override val actionLabel: String?
        get() = action?.label
}

@Immutable
sealed interface SnackbarKind {
    val icon: ImageVector

    @get:Composable
    @get:ReadOnlyComposable
    val iconTint: Color

    @Immutable
    data object Warning : SnackbarKind {
        override val icon: ImageVector = Icons.Outlined.Warning
        override val iconTint: Color
            @ReadOnlyComposable
            @Composable get() = MaterialTheme.colorScheme.error
    }

    @Immutable
    data object Success : SnackbarKind {
        override val icon: ImageVector = Icons.Outlined.Check
        override val iconTint: Color
            @ReadOnlyComposable
            @Composable get() = AppColor.success
    }
}

@Composable
fun AppSnackbarHost(snackbarBuilderFlow: SnackbarBuilderFlow = emptyFlow(), controller: SnackbarController = rememberSnackbarController()) {
    // Show Snackbars collected from snackbarBuilderFlow
    CollectLatestFromFlow(snackbarBuilderFlow) { builder ->
        controller.showReplacing { builder() }
    }

    SnackbarHost(controller.snackbarHostState) { snackbarData ->
        UpdateSnackbarVisibility(snackbarHostState = controller.snackbarHostState)
        AppSnackbar(visuals = snackbarData.visuals as AppSnackbarVisuals)
    }
}

@Composable
private fun UpdateSnackbarVisibility(
    snackbarVisibility: SnackbarVisibility = LocalSnackbarVisibility.current,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current
) {
    SideEffect { snackbarVisibility.set(true) }
    OnDispose {
        // Checking whether currentSnackbarData is necessary in cases where snackbar a is immediately replaced by snackbar b, where we don't
        // want a sequence of true -> false -> true, but just true.
        if (snackbarHostState.currentSnackbarData == null) {
            snackbarVisibility.set(false)
        }
    }
}

@Composable
fun AppSnackbar(visuals: AppSnackbarVisuals, modifier: Modifier = Modifier) {
    Snackbar(
        action = {
            visuals.action?.let { action ->
                TextButton(
                    onClick = action.callback
                ) {
                    Text(
                        text = action.label,
                        color = SnackbarDefaults.actionColor
                    )
                }
            }
        },
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            visuals.kind?.let { kind ->
                Icon(imageVector = kind.icon, contentDescription = null, tint = kind.iconTint)
                Spacer(modifier = Modifier.width(10.dp))
            }
            if (visuals.msg is AnnotatedString) {
                Text(text = visuals.msg)
            } else {
                Text(text = visuals.message)
            }
        }
    }
}
