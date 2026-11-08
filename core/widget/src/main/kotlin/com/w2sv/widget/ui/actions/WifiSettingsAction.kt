package com.w2sv.widget.ui.actions

import android.content.Context
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity
import com.w2sv.common.android.SettingsIntents

/** Glance's activity action requires an explicit component, including for system settings. */
internal fun wifiSettingsAction(context: Context): Action? {
    val intent = SettingsIntents.wifi
    val target = intent.resolveActivity(context.packageManager) ?: return null
    return actionStartActivity(intent.setComponent(target))
}
