package com.w2sv.widget.ui.actions

import android.content.Context
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import com.w2sv.common.android.SettingsIntents
import com.w2sv.domain.wifiproperty.WifiPropertyResolutionError
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentation
import com.w2sv.widget.ui.actions.clipboard.CopyToClipboardActivity

/** Constructs Glance actions for widget interactions without executing them. */
internal object WidgetActionFactory {
    fun refresh(): Action =
        actionRunCallback<RefreshWidgetAction>()

    /** Glance requires an explicit component, including for system settings. */
    fun wifiSettings(context: Context): Action? {
        val intent = SettingsIntents.wifi
        val target = intent.resolveActivity(context.packageManager) ?: return null
        return actionStartActivity(intent.setComponent(target))
    }

    fun property(context: Context, presentation: WifiPropertyPresentation): Action? {
        when (presentation.resolutionError) {
            WifiPropertyResolutionError.NoLocationAccessPermission,
            WifiPropertyResolutionError.GpsDisabled -> return actionStartActivity(locationAccessActivityIntent(context))
            null -> Unit
            else -> return null
        }
        if (presentation.value.isBlank()) return null

        val label = presentation.label.run { subscript?.let { "$text $it" } ?: text }
        return actionStartActivity(
            CopyToClipboardActivity.Payload
                .intent(label, presentation.value)
                .setClass(context, CopyToClipboardActivity::class.java)
        )
    }
}
