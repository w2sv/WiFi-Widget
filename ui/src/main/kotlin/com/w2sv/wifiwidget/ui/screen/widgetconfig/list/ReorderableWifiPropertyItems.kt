package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.SegmentedListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Interpolatable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import com.w2sv.composed.material3.rememberSnackbarController
import com.w2sv.composed.ui.thenIf
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.NestedSettingsTokens
import com.w2sv.wifiwidget.ui.designsystem.SegmentedListTokens
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigItemLayout
import com.w2sv.wifiwidget.ui.designsystem.configlist.SegmentedConfigListTokens
import com.w2sv.wifiwidget.ui.designsystem.configlist.SubSettings
import com.w2sv.wifiwidget.ui.location.capability.access.LocalLocationAccessCapability
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.screen.widgetconfig.model.wifiPropertyConfigItem
import com.w2sv.wifiwidget.ui.screen.widgetconfig.model.wifiPropertySubSettingItems
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState

fun LazyListScope.reorderableWifiPropertyItems(
    config: WidgetConfig,
    updateConfig: UpdateWidgetConfig,
    showDialog: (WidgetConfigDialog) -> Unit,
    reorderableState: ReorderableLazyListState
) {
    itemsIndexed(config.supportedProperties, key = { _, property -> property.protoId }) { index, property ->
        val hapticFeedback = LocalHapticFeedback.current

        ReorderableItem(
            state = reorderableState,
            key = property.protoId,
            modifier = Modifier.padding(
                bottom = if (index == config.supportedProperties.lastIndex) {
                    WidgetConfigListDimens.sectionSpacing
                } else {
                    SegmentedListTokens.itemSpacing
                }
            )
        ) { isDragging ->
            val elevation by animateDpAsState(
                targetValue = if (isDragging) SegmentedConfigListTokens.draggedElevation else SegmentedListTokens.defaultElevation
            )
            SegmentedListItem(
                shapes = morphedListItemShapes(index, config.supportedProperties.size, isDragging),
                colors = SegmentedListTokens.colors(),
                elevation = ListItemDefaults.elevation(elevation),
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .testTag("wifi_property_${property.labelRes}")
                    .longPressDraggableHandle(onDragStarted = { hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) }),
                contentPadding = SegmentedConfigListTokens.contentPadding
            ) {
                val scope = rememberCoroutineScope()
                val snackbarController = rememberSnackbarController(LocalSnackbarHostState.current)
                val subSettings = wifiPropertySubSettingItems(property, config, updateConfig, scope, snackbarController)
                val expandedContent: (@Composable () -> Unit)? = subSettings?.let { elements ->
                    {
                        val modifier = Modifier.padding(bottom = NestedSettingsTokens.bottomMargin)
                        if (property is WifiProperty.IpProperty) {
                            IpSubSettings(elements = elements, modifier = modifier)
                        } else {
                            SubSettings(elements = elements, modifier = modifier)
                        }
                    }
                }
                ConfigItemLayout(
                    item = wifiPropertyConfigItem(
                        property = property,
                        config = config,
                        updateConfig = updateConfig,
                        showDialog = showDialog,
                        locationAccess = LocalLocationAccessCapability.current,
                        scope = scope,
                        snackbarController = snackbarController
                    ),
                    padStartIfNoExpandButton = true,
                    shakeOnRejectedChange = true,
                    expandButtonModifier = Modifier.thenIf(property is WifiProperty.IpProperty) {
                        testTag("expand_ip_settings_${property.labelRes}")
                    },
                    expandedContent = expandedContent
                )
            }
        }
    }
}

@Composable
private fun morphedListItemShapes(
    index: Int,
    elementCount: Int,
    isDragging: Boolean
): ListItemShapes {
    val defaultShapes = ListItemDefaults.segmentedShapes(index = index, count = elementCount)
    val morphProgress by animateFloatAsState(
        targetValue = if (isDragging) 1f else 0f,
        animationSpec = tween(150)
    )
    val animatedShape = Interpolatable.lerp(defaultShapes.shape, defaultShapes.draggedShape, morphProgress) as Shape
    return defaultShapes.copy(shape = animatedShape)
}
