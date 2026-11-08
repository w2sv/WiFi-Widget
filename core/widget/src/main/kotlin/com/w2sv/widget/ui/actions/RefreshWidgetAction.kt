package com.w2sv.widget.ui.actions

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.w2sv.widget.di.glanceWidgetEntryPoint

internal class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        glanceWidgetEntryPoint(context)
            .workScheduler()
            .enqueueImmediateRefresh()
    }
}
