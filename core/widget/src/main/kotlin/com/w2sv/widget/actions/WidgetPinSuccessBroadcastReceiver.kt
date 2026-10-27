package com.w2sv.widget.actions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.w2sv.common.di.qualifier.AppDefaultScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Invoked on successful widget pins triggered from within the app. Emits via [WidgetPinSuccess].
 */
@AndroidEntryPoint
internal class WidgetPinSuccessBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var widgetPinSuccess: WidgetPinSuccess

    @Inject
    @AppDefaultScope
    lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        scope.launch {
            try {
                widgetPinSuccess.emit()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
