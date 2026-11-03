package com.w2sv.widget.ui.properties

import android.content.Context
import android.widget.RemoteViews
import com.w2sv.core.widget.R
import com.w2sv.domain.networking.NetworkAvailabilitySource
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import com.w2sv.widget.ui.properties.renderdata.WifiPropertyRenderData
import com.w2sv.widget.ui.properties.renderdata.WifiPropertyRenderDataProvider
import com.w2sv.widget.utils.logging.LoggingRemoteViewsFactory
import com.w2sv.widget.utils.remoteViews
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WifiPropertyRemoteViewsFactory @Inject internal constructor(
    @ApplicationContext private val context: Context,
    private val renderDataProvider: WifiPropertyRenderDataProvider,
    private val widgetConfigSource: WidgetConfigSource,
    private val remoteNetworkDataRepository: RemoteNetworkDataRepository,
    private val networkAvailabilitySource: NetworkAvailabilitySource,
    private val workScheduler: WifiWidgetWorkScheduler
) : LoggingRemoteViewsFactory() {

    private lateinit var data: WifiPropertyRenderData

    override fun onCreate() {
        super.onCreate()
        // Android may recreate a collection service without an APPWIDGET_UPDATE broadcast.
        // Only enqueue work here; RemoteViewsFactory callbacks must never perform HTTP.
        // Reconcile on every adapter creation; only a displayable egress can enqueue recovery.
        val needsRecovery = remoteNetworkDataRepository.needsRecovery(widgetConfigSource.blockingSnapshot().remoteDataConfig)
        if (needsRecovery && networkAvailabilitySource().canRefreshWidgetRemoteData) {
            workScheduler.enqueueRecovery()
        }
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
        data.presentations.size

    override fun getViewAt(position: Int): RemoteViews {
        // Keep the displayed text and its fill-in intent bound to the same immutable dataset.
        val snapshot = data
        return remoteViews(context, snapshot.layout) {
            val presentation = snapshot.presentations.getOrNull(position) ?: return@remoteViews
            inflatePropertyLayout(
                presentation = presentation,
                widgetColors = snapshot.colors,
                fontSize = snapshot.fontSize
            )
        }
    }

    override fun getLoadingView(): RemoteViews =
        remoteViews(context, R.layout.loading) {
            setTextColor(R.id.loading_tv, data.colors.secondary)
        }

    override fun getViewTypeCount(): Int =
        2 // Because of the 2 property layouts; If set to 1, switching to the other layout does not work

    override fun getItemId(position: Int): Long =
        if (position in data.presentations.indices) position.toLong() else -1L

    // Rows can move or change value on refresh, so their positions are not stable identities.
    override fun hasStableIds(): Boolean =
        false
}
