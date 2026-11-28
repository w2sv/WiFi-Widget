package com.w2sv.widget.ui.content

import android.content.Context
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.core.text.subscript
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.layout.wrapContentHeight
import com.w2sv.core.widget.R
import com.w2sv.domain.wifiproperty.presentation.SubscriptableText

/** The only native View used in property rows; Glance Text cannot apply a SubscriptSpan. */
@Composable
internal fun NativeSubscriptLabel(
    label: SubscriptableText,
    fontSize: Float,
    color: Int,
    modifier: GlanceModifier = GlanceModifier
) {
    val context = LocalContext.current
    AndroidRemoteViews(
        remoteViews = nativeSubscriptLabelViews(context, label, fontSize, color),
        modifier = modifier.wrapContentHeight()
    )
}

internal fun nativeSubscriptLabelViews(
    context: Context,
    label: SubscriptableText,
    fontSize: Float,
    color: Int
): RemoteViews =
    RemoteViews(context.packageName, R.layout.subscript_property_label).apply {
        setTextViewText(R.id.subscript_property_label, label.asSpannedLabel())
        setTextViewTextSize(R.id.subscript_property_label, TypedValue.COMPLEX_UNIT_SP, fontSize)
        setTextColor(R.id.subscript_property_label, color)
    }

internal fun SubscriptableText.asSpannedLabel() =
    buildSpannedString {
        append(text)
        subscript?.let { subscript { scale(0.8f) { append(it) } } }
    }
