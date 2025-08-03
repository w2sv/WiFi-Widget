package com.w2sv.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.w2sv.common.utils.toMapString
import slimber.log.i

/**
 * An [AppWidgetProvider] that logs upon invocation of its callbacks.
 */
abstract class LoggingAppWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        i {
            "${this::class.simpleName}.onReceive | Action=${intent.action} | Extras=${intent.extras?.toMapString()} | Flags=${intent.flags} | Data=${intent.data}"
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        i { "${this::class.simpleName}.onEnabled" }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        i { "${this::class.simpleName}.onDisabled" }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        i { "${this::class.simpleName}.onAppWidgetOptionsChanged | newOptions=${newOptions?.toMapString()}" }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        i { "${this::class.simpleName}.onUpdate | appWidgetIds=${appWidgetIds.toList()} | ${Thread.currentThread()}" }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        i { "${this::class.simpleName}.onDeleted | appWidgetIds=${appWidgetIds?.toList()}" }
    }

    override fun onRestored(
        context: Context?,
        oldWidgetIds: IntArray?,
        newWidgetIds: IntArray?
    ) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
        i { "${this::class.simpleName}.onRestored | oldWidgetIds=${oldWidgetIds?.toList()} | newWidgetIds=${newWidgetIds?.toList()}" }
    }
}
