package com.w2sv.wifiwidget.ui.screen.home.components.wifistatus

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.android.SettingsIntents
import com.w2sv.composed.ui.thenIf
import com.w2sv.core.resources.R
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.wifiproperty.WifiPropertyResolutionError
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentation
import com.w2sv.wifiwidget.ui.designsystem.SegmentedListTokens
import com.w2sv.wifiwidget.ui.screen.home.HomeScreenTokens
import com.w2sv.wifiwidget.ui.screen.home.model.wifistate.WifiState
import com.w2sv.wifiwidget.ui.util.toAnnotatedString

@Composable
fun WifiInformationList(
    wifiState: WifiState,
    modifier: Modifier = Modifier,
    fillAvailableHeight: Boolean = true
) {
    val presentations = wifiState.asConnectedOrNull?.propertyPresentations.orEmpty()
    val segmentCount = presentations.size + 1

    Column(modifier = modifier) {
        WifiStatusSegment(status = wifiState.status, segmentCount = segmentCount)

        // Keep the list for connected-state removals, including the last row; reset it on disconnect to avoid stale rows.
        key(wifiState is WifiState.Connected) {
            WifiPropertyList(
                presentations = presentations,
                segmentCount = segmentCount,
                modifier = Modifier
                    .fillMaxWidth()
                    .thenIf(fillAvailableHeight) { weight(1f) }
            )
        }
    }
}

@Composable
private fun WifiStatusSegment(status: WifiStatus, segmentCount: Int) {
    val context = LocalContext.current
    val settingsActionLabel = stringResource(R.string.go_to_wifi_settings_cd)

    SegmentedListItem(
        onClick = { context.startActivity(SettingsIntents.wifi) },
        shapes = ListItemDefaults.segmentedShapes(index = 0, count = segmentCount),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            leadingContentColor = MaterialTheme.colorScheme.primary,
            trailingContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        elevation = ListItemDefaults.elevation(SegmentedListTokens.defaultElevation),
        leadingContent = {
            Crossfade(targetState = status) { displayedStatus ->
                Icon(
                    painter = painterResource(displayedStatus.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        trailingContent = { Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, null) },
        modifier = Modifier
            .fillMaxWidth()
            .semantics { onClick(label = settingsActionLabel, action = null) }
    ) {
        Crossfade(targetState = status) { displayedStatus ->
            Text(stringResource(displayedStatus.labelRes), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun WifiPropertyList(
    presentations: List<WifiPropertyPresentation>,
    segmentCount: Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(top = SegmentedListTokens.itemSpacing),
        contentPadding = PaddingValues(bottom = HomeScreenTokens.scrollContentBottomPadding),
        verticalArrangement = Arrangement.spacedBy(SegmentedListTokens.itemSpacing)
    ) {
        itemsIndexed(
            items = presentations,
            key = { _, presentation -> "${presentation.label.text}:${presentation.label.subscript.orEmpty()}" }
        ) { index, presentation ->
            WifiPropertySegment(presentation = presentation, index = index, segmentCount = segmentCount)
        }
    }
}

@Composable
private fun LazyItemScope.WifiPropertySegment(
    presentation: WifiPropertyPresentation,
    index: Int,
    segmentCount: Int
) {
    val renderedLabel = remember(presentation.label) { presentation.label.toAnnotatedString(12.sp) }
    val actionLabel = when (presentation.resolutionError) {
        null -> stringResource(R.string.copy_value_to_clipboard_cd)
        WifiPropertyResolutionError.NoLocationAccessPermission -> stringResource(R.string.request_location_access_cd)
        WifiPropertyResolutionError.GpsDisabled -> stringResource(R.string.open_location_settings_cd)
        WifiPropertyResolutionError.BatterySaverRestricted -> null
    }
    val shapes = ListItemDefaults.segmentedShapes(index = index + 1, count = segmentCount)
    val colors = SegmentedListTokens.colors()
    val elevation = ListItemDefaults.elevation(SegmentedListTokens.defaultElevation)
    val itemModifier = Modifier.fillMaxWidth().animateItem()
    // Preserve the value animation when the row switches between clickable and non-clickable overloads.
    val propertyContent = remember {
        movableContentOf<WifiPropertyPresentation, AnnotatedString> { currentPresentation, currentLabel ->
            PropertyDisplay(
                presentation = currentPresentation,
                label = currentLabel,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (actionLabel == null) {
        SegmentedListItem(
            shapes = shapes,
            colors = colors,
            elevation = elevation,
            modifier = itemModifier
        ) {
            propertyContent(presentation, renderedLabel)
        }
    } else {
        val onClickScope = rememberPropertyOnClickScope()
        val scope = rememberCoroutineScope()
        SegmentedListItem(
            onClick = {
                onClickScope.run {
                    onPropertyClick(
                        scope = scope,
                        label = renderedLabel,
                        value = presentation.value,
                        resolutionError = presentation.resolutionError
                    )
                }
            },
            shapes = shapes,
            colors = colors,
            elevation = elevation,
            modifier = itemModifier.semantics { onClick(label = actionLabel, action = null) }
        ) {
            propertyContent(presentation, renderedLabel)
        }
    }
}
