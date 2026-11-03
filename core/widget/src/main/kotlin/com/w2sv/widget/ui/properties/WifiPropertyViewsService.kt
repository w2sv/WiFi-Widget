package com.w2sv.widget.ui.properties

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViewsService
import androidx.core.net.toUri
import com.w2sv.androidutils.content.intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Provider

/** Supplies a fresh factory when Android registers a new adapter Intent; Android owns equivalent-Intent reuse. */
@AndroidEntryPoint
internal class WifiPropertyViewsService : RemoteViewsService() {

    @Inject
    lateinit var wifiPropertyRemoteViewsFactoryProvider: Provider<WifiPropertyRemoteViewsFactory>

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        wifiPropertyRemoteViewsFactoryProvider.get()

    companion object {
        fun intent(context: Context, widgetId: Int): Intent =
            intent<WifiPropertyViewsService>(context).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                data = toUri(Intent.URI_INTENT_SCHEME).toUri()
            }
    }
}
