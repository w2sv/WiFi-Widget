package com.w2sv.widget.ui.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.w2sv.domain.model.widget.FontSize
import com.w2sv.domain.model.widget.WidgetColors
import com.w2sv.domain.model.widget.WifiPropertyValueAlignment
import com.w2sv.domain.model.wifiproperty.viewdata.SubscriptableText
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewData
import com.w2sv.widget.actions.CopyPropertyToClipboardActivity
import com.w2sv.widget.ui.theme.toColorProvider

@Composable
internal fun WifiPropertyList(
    properties: List<WifiPropertyViewData>,
    alignment: WifiPropertyValueAlignment,
    colors: WidgetColors,
    fontSize: FontSize,
    modifier: GlanceModifier = GlanceModifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        items(
            items = properties,
            itemId = { it.hashCode().toLong() }
        ) { property ->
            WifiPropertyRow(
                property = property,
                alignment = alignment,
                colors = colors,
                fontSize = fontSize
            )
        }
    }
}

@Composable
private fun WifiPropertyRow(
    property: WifiPropertyViewData,
    alignment: WifiPropertyValueAlignment,
    colors: WidgetColors,
    fontSize: FontSize
) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(
                actionStartActivity(
                    CopyPropertyToClipboardActivity.intent(
                        context = context,
                        propertyLabel = property.label.text,
                        propertyValue = property.value
                    )
                )
            )
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            PropertyLabel(
                label = property.label,
                color = colors.primary,
                fontSize = fontSize,
                modifier = if (alignment == WifiPropertyValueAlignment.Left) {
                    GlanceModifier.width(100.dp)
                } else {
                    GlanceModifier
                }
            )

            if (alignment == WifiPropertyValueAlignment.Right) {
                Spacer(GlanceModifier.defaultWeight())
            }

            Text(
                text = property.value,
                modifier = if (alignment == WifiPropertyValueAlignment.Left) {
                    GlanceModifier.defaultWeight()
                } else {
                    GlanceModifier
                },
                style = TextStyle(
                    color = colors.secondary.toColorProvider(),
                    fontSize = fontSize.value.sp,
                    textAlign = if (alignment == WifiPropertyValueAlignment.Right) TextAlign.End else TextAlign.Start
                ),
                maxLines = 3
            )
        }

        if (property.subValues.isNotEmpty()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.End
            ) {
                property.subValues.forEachIndexed { index, value ->
                    if (index > 0) Spacer(GlanceModifier.width(6.dp))
                    Text(
                        text = value,
                        modifier = GlanceModifier
                            .background(colors.subPropertyBackground.toColorProvider())
                            .cornerRadius(12.dp)
                            .padding(horizontal = 4.dp),
                        style = TextStyle(
                            color = colors.secondary.toColorProvider(),
                            fontSize = fontSize.subscriptSize.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun PropertyLabel(
    label: SubscriptableText,
    color: Int,
    fontSize: FontSize,
    modifier: GlanceModifier
) {
    Row(
        modifier = modifier.padding(end = 6.dp),
        verticalAlignment = Alignment.Vertical.Bottom
    ) {
        Text(
            text = label.text,
            style = TextStyle(
                color = color.toColorProvider(),
                fontSize = fontSize.value.sp
            )
        )
        label.subscript?.let {
            Text(
                text = it,
                style = TextStyle(
                    color = color.toColorProvider(),
                    fontSize = fontSize.subscriptSize.sp
                )
            )
        }
    }
}
