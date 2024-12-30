package com.w2sv.widget.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.IntDef

/**
 * Copy of hidden [PendingIntent].Flags.
 */
@IntDef(
    flag = true,
    value = [
        PendingIntent.FLAG_ONE_SHOT,
        PendingIntent.FLAG_NO_CREATE,
        PendingIntent.FLAG_CANCEL_CURRENT,
        PendingIntent.FLAG_UPDATE_CURRENT,
        PendingIntent.FLAG_IMMUTABLE,
        PendingIntent.FLAG_MUTABLE,
        PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT,

        Intent.FILL_IN_ACTION,
        Intent.FILL_IN_DATA,
        Intent.FILL_IN_CATEGORIES,
        Intent.FILL_IN_COMPONENT,
        Intent.FILL_IN_PACKAGE,
        Intent.FILL_IN_SOURCE_BOUNDS,
        Intent.FILL_IN_SELECTOR,
        Intent.FILL_IN_CLIP_DATA
    ]
)
@Retention(AnnotationRetention.SOURCE)
private annotation class PendingIntentFlags

fun activityPendingIntent(
    context: Context,
    intent: Intent,
    @PendingIntentFlags flags: Int,
    requestCode: Int = -1
): PendingIntent =
    PendingIntent.getActivity(context, requestCode, intent, flags)
