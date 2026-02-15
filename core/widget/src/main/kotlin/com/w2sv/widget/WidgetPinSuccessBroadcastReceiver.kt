package com.w2sv.widget

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.w2sv.androidutils.content.intent
import com.w2sv.common.di.AppDefaultScope
import com.w2sv.widget.di.EmitWidgetPinSuccess
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Invoked on successful widget pins triggered from within the app. Emits via [EmitWidgetPinSuccess].
 */
@AndroidEntryPoint
internal class WidgetPinSuccessBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var emitWidgetPinSuccess: EmitWidgetPinSuccess

    @Inject
    @AppDefaultScope
    lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        scope.launch {
            try {
                emitWidgetPinSuccess()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun pendingIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                1447,
                intent<WidgetPinSuccessBroadcastReceiver>(context),
                PendingIntent.FLAG_IMMUTABLE
            )
    }
}
