package com.w2sv.wifiwidget.ui.screen.widgetconfig

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.w2sv.composed.material3.rememberSnackbarController
import com.w2sv.composed.runtime.CollectLatestFromFlow
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.Easing
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.designsystem.kind
import com.w2sv.wifiwidget.ui.util.OnVisibilityStateChange
import com.w2sv.wifiwidget.ui.util.snackbar.LocalSnackbarVisibility
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first

private enum class EditingFabButton(@StringRes val labelRes: Int, val imageVector: ImageVector, val diameter: Dp) {
    Reset(
        labelRes = R.string.reset,
        imageVector = Icons.Default.Refresh,
        diameter = 64.dp
    ),
    Apply(
        labelRes = R.string.apply,
        imageVector = Icons.Default.Check,
        diameter = 80.dp
    );

    @ReadOnlyComposable
    @Composable
    fun colors(): Pair<Color, Color> =
        when (this) {
            Reset -> colorScheme.surfaceContainerHigh to colorScheme.onSurface
            Apply -> colorScheme.primaryContainer to colorScheme.onPrimaryContainer
        }
}

@Composable
fun EditingFabButtonRow(configEditState: ConfigEditState, modifier: Modifier = Modifier) {
    val isDirty = configEditState.isDirty()
    val snackbarVisibility = LocalSnackbarVisibility.current
    val snackbarHostState = LocalSnackbarHostState.current
    val snackbarData = snackbarHostState.currentSnackbarData
    val snackbarBlocksFabs = when {
        snackbarData != null -> snackbarData.kind != SnackbarKind.Warning
        snackbarVisibility.isVisible -> snackbarVisibility.kind != SnackbarKind.Warning
        else -> false
    }
    var fabButtonsVisible by remember { mutableStateOf(false) }

    CoordinateFabsAndSnackbarVisibility(
        configEditState = configEditState,
        fabButtonsVisible = { fabButtonsVisible }
    )

    EditingFabVisibility(
        visible = isDirty && !snackbarBlocksFabs,
        onVisibilityChanged = { fabButtonsVisible = it }
    ) {
        Row(
            // Keep spacing inside AnimatedVisibility so an absent FAB leaves no Scaffold slot height.
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EditingFabButton(
                button = EditingFabButton.Reset,
                onClick = configEditState.revertChanges
            )
            EditingFabButton(
                button = EditingFabButton.Apply,
                onClick = configEditState.commitChanges
            )
        }
    }
}

/**
 * Coordinates visibility between the FABs and snackbars.
 *
 * Dismisses a snackbar when editing starts and waits for the FAB exit animation
 * before showing an Apply confirmation. FAB entrance follows actual snackbar visibility.
 */
@Composable
private fun CoordinateFabsAndSnackbarVisibility(configEditState: ConfigEditState, fabButtonsVisible: () -> Boolean) {
    val snackbarController = rememberSnackbarController(LocalSnackbarHostState.current)

    // Dismiss snackbars on editable config changes
    LaunchedEffect(configEditState) {
        snapshotFlow { configEditState.editableConfig() }
            .drop(1)
            .collect { snackbarController.dismissCurrent() }
    }

    // The applied-state flow can still report dirty when this commit event arrives.
    CollectLatestFromFlow(configEditState.changesHaveBeenCommitted) { committedConfig ->
        if (configEditState.editableConfig() != committedConfig) return@CollectLatestFromFlow
        snackbarController.dismissCurrent()
        suspendUntil { !fabButtonsVisible() || configEditState.editableConfig() != committedConfig }
        if (configEditState.editableConfig() != committedConfig) return@CollectLatestFromFlow
        snackbarController.show {
            AppSnackbarVisuals(
                msg = getString(R.string.updated_widget_configuration),
                kind = SnackbarKind.Success
            )
        }
    }
}

private suspend fun suspendUntil(condition: () -> Boolean) {
    snapshotFlow { condition() }.first { it }
}

@Composable
private fun EditingFabVisibility(
    visible: Boolean,
    onVisibilityChanged: (Boolean) -> Unit,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it / 2 },
            animationSpec = tween(easing = Easing.Anticipate)
        ) + fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { it / 2 },
            animationSpec = tween(easing = Easing.Anticipate)
        ) + fadeOut()
    ) {
        OnVisibilityStateChange(transition, onVisibilityChanged)
        content()
    }
}

@Composable
private fun EditingFabButton(
    button: EditingFabButton,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = button.colors()

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(button.diameter),
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = button.imageVector,
                contentDescription = null
            )
            Text(text = stringResource(button.labelRes))
        }
    }
}
