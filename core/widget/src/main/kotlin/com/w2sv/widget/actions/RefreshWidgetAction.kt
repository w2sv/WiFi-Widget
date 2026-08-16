package com.w2sv.widget.actions

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.w2sv.widget.di.GlanceWidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors

internal class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        EntryPointAccessors.fromApplication(
            context,
            GlanceWidgetEntryPoint::class.java
        ).workScheduler().enqueueImmediateRefresh()
    }
}
