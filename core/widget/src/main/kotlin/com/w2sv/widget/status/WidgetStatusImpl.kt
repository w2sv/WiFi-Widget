package com.w2sv.widget.status

import android.appwidget.AppWidgetManager
import android.content.Context
import com.w2sv.androidutils.appwidget.appWidgetIds
import com.w2sv.domain.widget.WidgetStatus
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.widget.di.WidgetPinSuccessFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharedFlow

/**
 * Reads installed widget IDs from Android on demand and exposes the app's pin-success stream.
 *
 * The IDs are not cached because widget hosts can add or remove instances while this process runs.
 */
internal class WidgetStatusImpl @Inject constructor(
    private val appWidgetManager: AppWidgetManager,
    @ApplicationContext private val context: Context,
    @WidgetPinSuccessFlow override val pinSuccesses: SharedFlow<Unit>
) : WidgetStatus {

    override val ids: IntArray
        get() = appWidgetManager.appWidgetIds<WifiWidgetProvider>(context)

    override val hasInstalledWidgets: Boolean
        get() = ids.isNotEmpty()
}
