package com.w2sv.wifiwidget.ui.screen.locationaccess.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.CapturedAnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.w2sv.composed.ui.thenIf
import com.w2sv.composed.ui.thenIfNotNull
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessScreenTokens
import com.w2sv.wifiwidget.ui.screen.locationaccess.state.LocationAccessScreenActions

@Composable
internal fun LocationAccessActionPanel(
    presentation: LocationAccessActionPanelPresentation,
    actions: LocationAccessScreenActions,
    modifier: Modifier = Modifier,
    standalone: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = if (standalone) RoundedCornerShape(24.dp) else RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .thenIf(standalone) { verticalScroll(rememberScrollState()) }
                .thenIf(!standalone) {
                    windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + Horizontal))
                }
                .padding(LocationAccessScreenTokens.actionPanelContentPadding),
            verticalArrangement = Arrangement.spacedBy(LocationAccessScreenTokens.actionSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LocationAccessStatusSection(presentation.status, actions.onOpenLocationSettings)
            LocationAccessActionButtons(presentation, actions)
            if (presentation.showOnboardingHint) {
                Text(
                    text = stringResource(R.string.location_access_later),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LocationAccessStatusSection(status: LocationAccessPresentationStatus?, onOpenLocationSettings: () -> Unit) {
    AnimatedContent(
        targetState = status,
        transitionSpec = { fadeIn(tween(180, delayMillis = 120)) togetherWith fadeOut(tween(120)) },
        modifier = Modifier.fillMaxWidth(),
        label = "Location access status"
    ) { displayedStatus ->
        if (displayedStatus != null) {
            LocationAccessStatusRow(
                status = displayedStatus,
                onOpenLocationSettings = onOpenLocationSettings,
                modifier = Modifier.padding(bottom = LocationAccessScreenTokens.statusBottomPadding)
            )
        }
    }
}

@Composable
private fun LocationAccessStatusRow(
    status: LocationAccessPresentationStatus,
    onOpenLocationSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onClick = onOpenLocationSettings.takeIf { status.opensLocationSettings }
    val description = status.description?.let { stringResource(it) }
    val (imageVector, tint) = if (status.positive) {
        Icons.Default.Check to MaterialTheme.colorScheme.primary
    } else {
        Icons.Default.Info to MaterialTheme.colorScheme.tertiary
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .thenIfNotNull(onClick) {
                this
                    .heightIn(min = LocationAccessScreenTokens.statusMinHeight)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(onClickLabel = stringResource(R.string.open_location_settings), role = Role.Button, onClick = it)
            },
        horizontalArrangement = Arrangement.spacedBy(LocationAccessScreenTokens.statusContentSpacing),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(LocationAccessScreenTokens.statusIconSize)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(status.title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            if (description != null) {
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (onClick != null) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(LocationAccessScreenTokens.statusChevronSize)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
private fun LocationAccessActionButtons(presentation: LocationAccessActionPanelPresentation, actions: LocationAccessScreenActions) {
    Column(
        verticalArrangement = Arrangement.spacedBy(LocationAccessScreenTokens.actionSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PrimaryLocationAccessActionButton(presentation.primaryAction, presentation.actionsEnabled, actions)
        SecondaryLocationAccessActionButton(presentation.secondaryAction, presentation.actionsEnabled, actions)
        AppSettingsLocationAccessActionButton(presentation.showAppSettingsAction, presentation.actionsEnabled, actions)
    }
}

@Composable
private fun PrimaryLocationAccessActionButton(
    action: LocationAccessActionPanelPresentation.Action,
    actionsEnabled: Boolean,
    actions: LocationAccessScreenActions
) {
    Button(
        onClick = actionCallback(action.kind, actions),
        enabled = actionsEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = LocationAccessScreenTokens.actionButtonMinHeight)
    ) {
        AnimatedLocationActionLabel(action.labelRes)
    }
}

@Composable
private fun SecondaryLocationAccessActionButton(
    action: LocationAccessActionPanelPresentation.Action?,
    actionsEnabled: Boolean,
    actions: LocationAccessScreenActions
) {
    val transition = updateTransition(targetState = action, label = "Secondary location access action")
    // Remove the empty slot after exit so the surrounding Column does not retain a spacing gap.
    if (transition.currentState == null && transition.targetState == null) return

    val density = LocalDensity.current
    transition.AnimatedContent(
        contentKey = { it != null },
        modifier = Modifier.fillMaxWidth(),
        transitionSpec = {
            val enter = fadeIn(tween(180)) + expandVertically(tween(180)) +
                slideInVertically(tween(180)) { with(density) { 8.dp.roundToPx() } }
            val exit = fadeOut(tween(120)) + shrinkVertically(tween(120))
            (enter togetherWith exit).using(null)
        }
    ) { displayedAction ->
        if (displayedAction != null) {
            OutlinedButton(
                onClick = actionCallback(displayedAction.kind, actions),
                enabled = actionsEnabled && action == displayedAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = LocationAccessScreenTokens.actionButtonMinHeight)
            ) {
                AnimatedLocationActionLabel(displayedAction.labelRes)
            }
        }
    }
}

@Composable
private fun AppSettingsLocationAccessActionButton(
    visible: Boolean,
    actionsEnabled: Boolean,
    actions: LocationAccessScreenActions
) {
    CapturedAnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)) + expandVertically(tween(180)),
        exit = fadeOut(tween(120)) + shrinkVertically(tween(120))
    ) {
        TextButton(onClick = actions.onOpenAppSettings, enabled = actionsEnabled) {
            Text(stringResource(R.string.manage_in_android_settings))
        }
    }
}

private fun actionCallback(kind: LocationAccessActionPanelPresentation.ActionKind, actions: LocationAccessScreenActions): () -> Unit =
    when (kind) {
        LocationAccessActionPanelPresentation.ActionKind.RequestForeground -> actions.onRequestForeground
        LocationAccessActionPanelPresentation.ActionKind.RequestBackground -> actions.onRequestBackground
        LocationAccessActionPanelPresentation.ActionKind.Continue -> actions.onContinue
        LocationAccessActionPanelPresentation.ActionKind.OpenAppSettings -> actions.onOpenAppSettings
    }

@Composable
private fun AnimatedLocationActionLabel(@StringRes labelRes: Int) {
    AnimatedContent(targetState = labelRes) { displayedLabel ->
        Text(stringResource(displayedLabel))
    }
}
