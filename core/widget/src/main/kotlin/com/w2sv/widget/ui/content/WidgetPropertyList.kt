package com.w2sv.widget.ui.content

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentWidth
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentation
import com.w2sv.widget.ui.actions.CopyPropertyToClipboardActivity
import com.w2sv.widget.ui.layout.PropertyLayout
import com.w2sv.widget.ui.state.WidgetPresentationState
import com.w2sv.widget.ui.state.WidgetPropertyRow
import com.w2sv.widget.ui.theme.WidgetDimens
import com.w2sv.widget.ui.theme.subPropertyText
import com.w2sv.widget.ui.theme.subscriptSize
import com.w2sv.widget.ui.theme.textSize
import com.w2sv.widget.ui.theme.toColorProvider
import com.w2sv.widget.ui.util.thenIfNotNull

@Composable
internal fun WidgetPropertyList(
    rows: List<WidgetPropertyRow>,
    state: WidgetPresentationState,
    layout: PropertyLayout,
    bottomClearance: Dp
) {
    LazyColumn(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(start = WidgetDimens.Properties.listStartPadding, end = WidgetDimens.Properties.listEndPadding)
    ) {
        item(itemId = Long.MAX_VALUE) {
            Spacer(GlanceModifier.height(WidgetDimens.Properties.listContentTopPadding))
        }
        items(rows, itemId = WidgetPropertyRow::id) { row ->
            PropertyRow(row, state, layout)
        }
        if (bottomClearance > 0.dp) {
            // Scroll content clears the fixed overlay without reducing the list viewport.
            item(itemId = Long.MIN_VALUE) {
                Spacer(GlanceModifier.height(bottomClearance))
            }
        }
    }
}

@Composable
private fun PropertyRow(
    row: WidgetPropertyRow,
    widgetPresentation: WidgetPresentationState,
    layout: PropertyLayout
) {
    val rowPresentation = row.presentation
    val context = LocalContext.current
    val labelModifier = layout.labelColumnWidth?.let { GlanceModifier.width(it) } ?: GlanceModifier.wrapContentWidth()

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .thenIfNotNull(row.presentation.copyIntent(context)) { clickable(actionStartActivity(it)) }
            .padding(
                top = WidgetDimens.Properties.rowVerticalPadding,
                bottom = WidgetDimens.Properties.rowVerticalPadding,
                end = WidgetDimens.Properties.scrollbarClearance
            )
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Vertical.Top) {
            // Glance has no max intrinsic width; compact rows cap labels to preserve value space.
            PropertyLabel(
                row = row,
                state = widgetPresentation,
                modifier = labelModifier,
                trailingPadding = WidgetDimens.Properties.labelTrailingPadding
            )
            Text(
                text = rowPresentation.value,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(
                    color = widgetPresentation.colors.secondary.toColorProvider(),
                    fontSize = widgetPresentation.fontSize.textSize.sp,
                    textAlign = widgetPresentation.valueAlign
                )
            )
        }
        if (rowPresentation.subValues.isNotEmpty()) {
            SubPropertyText(
                subProperties = rowPresentation.subValues,
                widgetPresentation = widgetPresentation,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(start = layout.subpropertyIndent)
            )
        }
    }
}

@Composable
private fun SubPropertyText(
    subProperties: List<String>,
    widgetPresentation: WidgetPresentationState,
    modifier: GlanceModifier = GlanceModifier
) {
    Text(
        text = subProperties.joinToString(" · "),
        modifier = modifier,
        style = TextStyle(
            color = widgetPresentation.colors.subPropertyText.toColorProvider(),
            fontSize = widgetPresentation.fontSize.subscriptSize.sp,
            textAlign = widgetPresentation.valueAlign
        )
    )
}

internal fun WifiPropertyPresentation.copyIntent(context: Context): Intent? {
    if (resolutionError != null || value.isBlank()) return null

    val propertyLabel = label.run { subscript?.let { "$text $it" } ?: text }

    return CopyPropertyToClipboardActivity.Property
        .intent(propertyLabel, value)
        .setClass(context, CopyPropertyToClipboardActivity::class.java)
}

@Composable
private fun PropertyLabel(
    row: WidgetPropertyRow,
    state: WidgetPresentationState,
    modifier: GlanceModifier,
    trailingPadding: Dp
) {
    val label = row.presentation.label
    if (label.subscript == null) {
        Text(
            text = label.text,
            modifier = modifier.padding(end = trailingPadding),
            style = TextStyle(color = state.colors.primary.toColorProvider(), fontSize = state.fontSize.textSize.sp),
            maxLines = 2
        )
    } else {
        NativeSubscriptLabel(
            label = label,
            fontSize = state.fontSize.textSize,
            color = state.colors.primary,
            modifier = modifier.padding(end = trailingPadding)
        )
    }
}
