package com.w2sv.widget.utils

import android.app.PendingIntent
import android.content.Context
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import com.w2sv.androidutils.appwidget.setColorFilter

internal fun RemoteViews.setTextView(
    @IdRes viewId: Int,
    text: CharSequence,
    size: Float? = null,
    @ColorInt color: Int? = null,
    isVisible: Boolean = true
) {
    setViewVisibility(viewId, isVisible) {
        setTextViewText(viewId, text)
        size?.let { setTextViewTextSize(viewId, COMPLEX_UNIT_SP, it) }
        color?.let { setTextColor(viewId, it) }
    }
}

internal fun RemoteViews.setViewVisibility(
    @IdRes viewId: Int,
    isVisible: Boolean,
    ifVisible: RemoteViews.() -> Unit = {}
) {
    setViewVisibility(
        viewId,
        if (isVisible) View.VISIBLE.also { ifVisible() } else View.GONE
    )
}

internal fun RemoteViews.setButton(
    @IdRes viewId: Int,
    isVisible: Boolean,
    @ColorInt color: Int,
    pendingIntent: PendingIntent
) {
    setViewVisibility(viewId, isVisible) {
        setColorFilter(viewId, color)
        setOnClickPendingIntent(viewId, pendingIntent)
    }
}

internal fun RemoteViews.setImageView(
    @IdRes viewId: Int,
    @DrawableRes srcId: Int,
    @ColorInt color: Int
) {
    setImageViewResource(viewId, srcId)
    setColorFilter(viewId, color)
}

internal fun remoteViews(context: Context, @LayoutRes layout: Int): RemoteViews =
    RemoteViews(context.packageName, layout)
