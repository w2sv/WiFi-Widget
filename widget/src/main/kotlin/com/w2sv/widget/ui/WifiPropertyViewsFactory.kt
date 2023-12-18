package com.w2sv.widget.ui

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.widget.data.appearance
import com.w2sv.widget.model.WidgetColors
import com.w2sv.widget.model.WidgetPropertyView
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import slimber.log.i
import javax.inject.Inject
import kotlin.properties.Delegates

class WifiPropertyViewsFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetRepository: WidgetRepository,
    private val viewDataFactory: WidgetWifiProperty.ViewData.Factory,
) : RemoteViewsService.RemoteViewsFactory {

    override fun onCreate() {}

    private lateinit var propertyViewData: List<WidgetPropertyView>
    private lateinit var widgetColors: WidgetColors

    private var nViewTypes by Delegates.notNull<Int>()

    override fun onDataSetChanged() {
        i { "${this::class.simpleName}.onDataSetChanged" }

        runBlocking {  // TODO
            propertyViewData = viewDataFactory(
                properties = widgetRepository.getEnabledWifiProperties(),
                ipSubPropertyEnablementMap = widgetRepository.getIPSubPropertyEnablementMap()
                    .getSynchronousMap()
            )
                .toList()
                .flatMap { valueViewData ->
                    when (valueViewData) {
                        is WidgetWifiProperty.ViewData.NonIP -> listOf(
                            WidgetPropertyView.Property(
                                valueViewData
                            )
                        )

                        is WidgetWifiProperty.ViewData.IPProperty -> buildList<WidgetPropertyView> {
                            add(WidgetPropertyView.Property(valueViewData))
                            valueViewData.prefixLengthText?.let {
                                add(WidgetPropertyView.PrefixLength(it))
                            }
                        }
                    }
                }

            nViewTypes = propertyViewData.map { it.javaClass }.toSet().size
            widgetColors = widgetRepository.appearance.last().getColors(context)
        }
    }

    override fun getCount(): Int = propertyViewData.size

    override fun getViewAt(position: Int): RemoteViews =
        propertyViewData[position].inflate(context.packageName, widgetColors)

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = nViewTypes

    override fun getItemId(position: Int): Long = propertyViewData[position].hashCode().toLong()

    override fun hasStableIds(): Boolean = true

    override fun onDestroy() {}
}
