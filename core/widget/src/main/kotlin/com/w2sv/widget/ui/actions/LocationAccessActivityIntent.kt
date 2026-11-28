package com.w2sv.widget.ui.actions

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.w2sv.widget.di.glanceWidgetEntryPoint

/** App-owned destination. Each call returns a fresh explicit intent without widget launch configuration. */
fun interface LocationAccessActivityIntentProvider {
    operator fun invoke(context: Context): Intent
}

internal fun locationAccessActivityIntent(context: Context): Intent =
    glanceWidgetEntryPoint(context).locationAccessActivityIntentProvider().widgetIntent(context)

private fun LocationAccessActivityIntentProvider.widgetIntent(context: Context): Intent =
    invoke(context).apply {
        // Keep one identity across Glance rows/widgets; otherwise Glance adds a per-row URI.
        data = "wifiwidget://${context.packageName}/location-access".toUri()
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
