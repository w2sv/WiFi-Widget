package com.w2sv.widget.ui.properties

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.text.SpannedString
import android.view.View
import android.widget.RemoteViews
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.core.text.subscript
import com.w2sv.androidutils.appwidget.setBackgroundColor
import com.w2sv.core.widget.R
import com.w2sv.domain.widget.FontSize
import com.w2sv.domain.widget.WidgetColors
import com.w2sv.domain.wifiproperty.presentation.SubscriptableText
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentation
import com.w2sv.widget.utils.setTextView

internal fun RemoteViews.inflatePropertyLayout(
    presentation: WifiPropertyPresentation,
    widgetColors: WidgetColors,
    fontSize: FontSize
) {
    // Label TextView
    setTextView(
        viewId = R.id.property_label_tv,
        text = presentation.label.toSpannedString(0.8f),
        size = fontSize.value,
        color = widgetColors.primary
    )

    // Value TextView
    setTextView(
        viewId = R.id.property_value_tv,
        text = presentation.value,
        size = fontSize.value,
        color = widgetColors.secondary
    )

    // Reapplied RemoteViews can retain an earlier row's click tag unless it is explicitly cleared.
    setOnClickFillInIntent(R.id.property_click_target, presentation.copyIntentOrNull())

    // IP sub property row
    if (presentation.subValues.isNotEmpty()) {
        displayIpSubValues(presentation.subValues, fontSize, widgetColors)
    } else {
        setViewVisibility(R.id.ip_sub_property_row, View.GONE)
    }
}

private fun WifiPropertyPresentation.copyIntentOrNull(): Intent? =
    if (resolutionError != null) {
        null
    } else {
        CopyPropertyToClipboardActivity.Property.intent(
            propertyLabel = label.clipboardLabel(),
            propertyValue = value
        )
    }

private fun SubscriptableText.clipboardLabel(): String =
    subscript?.let { "$text $it" } ?: text

private fun SubscriptableText.toSpannedString(subscriptScale: Float): SpannedString =
    buildSpannedString {
        append(text)
        subscript?.let { subscript { scale(subscriptScale) { append(it) } } }
    }

private fun RemoteViews.displayIpSubValues(
    values: List<String>,
    fontSize: FontSize,
    widgetColors: WidgetColors
) {
    setViewVisibility(R.id.ip_sub_property_row, View.VISIBLE)

    val subPropertyViewIterator = sequenceOf(R.id.ip_sub_property_tv_2, R.id.ip_sub_property_tv_1).iterator()
    values.reversed().forEach { value ->
        val viewId = subPropertyViewIterator.next()
        setTextView(
            viewId = viewId,
            text = value,
            size = fontSize.subscriptSize,
            color = widgetColors.secondary
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setColorStateList(
                viewId,
                "setBackgroundTintList",
                ColorStateList.valueOf(widgetColors.subPropertyBackground)
            )
        } else {
            setBackgroundColor(
                viewId,
                widgetColors.subPropertyBackground
            )
        }
    }
    subPropertyViewIterator.forEachRemaining { setViewVisibility(it, View.GONE) }
}
