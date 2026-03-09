package com.w2sv.widget.ui.properties

import android.content.Context
import android.widget.RemoteViews
import com.w2sv.common.utils.log
import com.w2sv.core.widget.R
import com.w2sv.widget.utils.logging.LoggingRemoteViewsFactory
import com.w2sv.widget.utils.remoteViews
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class WifiPropertyRemoteViewsFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val renderDataProvider: WifiPropertyRenderDataProvider
) : LoggingRemoteViewsFactory() {

    private lateinit var data: WifiPropertyRenderData

    override fun onCreate() {
        super.onCreate()
        updateData()
    }

    override fun onDataSetChanged() {
        super.onDataSetChanged()
        updateData()
    }

    private fun updateData() {
        data = renderDataProvider(context)
    }

    override fun getCount(): Int =
        data.viewData.size

    override fun getViewAt(position: Int): RemoteViews =
        remoteViews(context, data.layout) {
            val viewData = data.viewData.getOrNull(position) ?: return@remoteViews
            inflatePropertyLayout(
                viewData = viewData.log { "Inflating property layout for $it" },
                widgetColors = data.colors,
                fontSize = data.fontSize
            )
        }

    override fun getLoadingView(): RemoteViews =
        remoteViews(context, R.layout.loading) {
            setTextColor(R.id.loading_tv, data.colors.secondary)
        }

    override fun getViewTypeCount(): Int =
        1

    override fun getItemId(position: Int): Long =
        data.viewData.getOrNull(position)?.hashCode()?.toLong() ?: -1L

    override fun hasStableIds(): Boolean =
        true
}
