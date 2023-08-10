package com.w2sv.widget.ui

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.common.utils.enumerationTag
import com.w2sv.data.model.WifiProperty
import com.w2sv.data.storage.WidgetRepository
import com.w2sv.widget.data.appearance
import com.w2sv.widget.model.WidgetColors
import com.w2sv.widget.model.WifiPropertyLayoutViewData
import dagger.hilt.android.qualifiers.ApplicationContext
import slimber.log.i
import javax.inject.Inject
import kotlin.properties.Delegates

class WifiPropertyViewsFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetRepository: WidgetRepository,
    private val valueGetterResources: WifiProperty.ValueGetterResources
) : RemoteViewsService.RemoteViewsFactory {

    override fun onCreate() {}

    private lateinit var propertyViewData: List<WifiPropertyLayoutViewData>
    private lateinit var widgetColors: WidgetColors

    private var nViewTypes by Delegates.notNull<Int>()

    override fun onDataSetChanged() {
        i { "${this::class.simpleName}.onDataSetChanged" }

        val ipSubProperties by lazy {
            widgetRepository.subWifiProperties.getSynchronousMap()
        }

        propertyViewData = buildList {
            widgetRepository.getSetWifiProperties().forEach {
                when (val value = it.getValue(valueGetterResources)) {
                    is WifiProperty.Value.Singular -> {
                        add(
                            WifiPropertyLayoutViewData.WifiProperty(
                                context.getString(it.viewData.labelRes), value.value
                            )
                        )
                    }

                    is WifiProperty.Value.IPAddresses -> {
                        val filteredAddresses = when (val ipProperty = value.property) {
                            is WifiProperty.IPv4 -> value.addresses
                            is WifiProperty.IPv6 -> value.addresses.filter { address ->
                                when {
                                    !ipSubProperties.getValue(ipProperty.includeLocal) -> !address.isLocal
                                    !ipSubProperties.getValue(ipProperty.includePublic) -> address.isLocal
                                    else -> true
                                }
                            }
                        }
                        filteredAddresses.forEachIndexed { i, address ->
                            add(
                                WifiPropertyLayoutViewData.WifiProperty(
                                    buildString {
                                        append(context.getString(it.viewData.labelRes))
                                        if (filteredAddresses.size > 1) {
                                            append(" ${enumerationTag(i)}")
                                        }
                                    },
                                    address.textualRepresentation
                                )
                            )
                            add(
                                WifiPropertyLayoutViewData.IPProperties(
                                    ipAddress = address,
                                    showPrefixLength = ipSubProperties.getValue(value.property.prefixLengthSubProperty)
                                )
                            )
                        }
                    }
                }
            }
        }
        nViewTypes = propertyViewData.map { it.javaClass }.toSet().size

        widgetColors = widgetRepository.appearance.getValueSynchronously().getColors(context)
    }

    override fun getCount(): Int = propertyViewData.size

    override fun getViewAt(position: Int): RemoteViews =
        propertyViewData[position].inflateView(context, widgetColors)

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = nViewTypes

    override fun getItemId(position: Int): Long = propertyViewData[position].hashCode().toLong()

    override fun hasStableIds(): Boolean = true

    override fun onDestroy() {}
}