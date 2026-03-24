package com.w2sv.widget.ui.properties

import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.w2sv.androidutils.appwidget.setBackgroundColor
import com.w2sv.core.widget.R
import com.w2sv.domain.model.widget.FontSize
import com.w2sv.domain.model.widget.WidgetColors
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewData
import com.w2sv.widget.ui.properties.CopyPropertyToClipboardActivity
import com.w2sv.widget.utils.setTextView

internal fun RemoteViews.inflatePropertyLayout(
    viewData: WifiPropertyViewData,
    widgetColors: WidgetColors,
    fontSize: FontSize
) {
    // Label TextView
    setTextView(
        viewId = R.id.property_label_tv,
        text = viewData.label.toSpannedString(0.8f),
        size = fontSize.value,
        color = widgetColors.primary
    )

    // Value TextView
    setTextView(
        viewId = R.id.property_value_tv,
        text = viewData.value,
        size = fontSize.value,
        color = widgetColors.secondary
    )

    // OnClickFillInIntent
    setOnClickFillInIntent(
        R.id.wifi_property_layout,
        CopyPropertyToClipboardActivity.Args.getIntent(
            propertyLabel = viewData.label.text,
            propertyValue = viewData.value
        )
    )

    // IP sub property row
    if (viewData.subValues.isNotEmpty()) {
        displayIpSubValues(viewData.subValues, fontSize, widgetColors)
    } else {
        setViewVisibility(R.id.ip_sub_property_row, View.GONE)
    }
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
