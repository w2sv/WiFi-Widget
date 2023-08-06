package com.w2sv.widget.ui

import android.content.Context
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.data.model.WifiProperty
import com.w2sv.data.networking.IPAddress
import com.w2sv.data.storage.WidgetRepository
import com.w2sv.widget.R
import com.w2sv.widget.data.appearance
import com.w2sv.widget.model.WidgetColors
import com.w2sv.widget.model.WifiPropertyLayoutViewData
import com.w2sv.widget.utils.setTextView
import dagger.hilt.android.qualifiers.ApplicationContext
import slimber.log.i
import javax.inject.Inject
import kotlin.properties.Delegates

class WifiPropertyViewsFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetRepository: WidgetRepository
) : RemoteViewsService.RemoteViewsFactory {

    private val valueGetterResources by lazy {
        WifiProperty.ValueGetterResources(context)
    }

    override fun onCreate() {}

    private lateinit var propertyViewData: List<WifiPropertyLayoutViewData>
    private lateinit var widgetColors: WidgetColors

    private var nViewTypes by Delegates.notNull<Int>()

    override fun onDataSetChanged() {
        i { "${this::class.simpleName}.onDataSetChanged" }

        propertyViewData = buildList {
            widgetRepository.getSetWifiProperties()
                .forEach {
                    when (val value = it.getValue(valueGetterResources)) {
                        is WifiProperty.Value.Singular -> {
                            add(
                                WifiPropertyLayoutViewData.WifiProperty(
                                    context.getString(it.viewData.labelRes),
                                    value.value
                                )
                            )
                        }

                        is WifiProperty.Value.IPAddresses -> {
                            value.addresses.forEachIndexed { i, address ->
                                add(
                                    WifiPropertyLayoutViewData.WifiProperty(
                                        context.getString(it.viewData.labelRes)
                                            .run {
                                                if (address.type == IPAddress.Type.V6 && !address.isLocal)
                                                    "$this ${i + 1}"
                                                else
                                                    this
                                            },
                                        address.textualRepresentation
                                    )
                                )
                                add(WifiPropertyLayoutViewData.IPProperties(address))
                            }
                        }
                    }
                }
        }
        nViewTypes = propertyViewData.map { it.javaClass }.toSet().size

        widgetColors = widgetRepository.appearance.getValueSynchronously().theme.getColors(context)
    }

    override fun getCount(): Int = propertyViewData.size

    override fun getViewAt(position: Int): RemoteViews =
        when (val viewData = propertyViewData[position]) {
            is WifiPropertyLayoutViewData.WifiProperty -> {
                RemoteViews(context.packageName, R.layout.wifi_property)
                    .apply {
                        setTextView(
                            viewId = R.id.property_label_tv,
                            text = viewData.label,
                            color = widgetColors.primary
                        )
                        setTextView(
                            viewId = R.id.property_value_tv,
                            text = viewData.value,
                            color = widgetColors.secondary
                        )
                    }
            }

            is WifiPropertyLayoutViewData.IPProperties -> {
                RemoteViews(context.packageName, R.layout.ip_properties)
                    .apply {
                        val propertyTVIterator = listOf(
                            R.id.ip_property_tv_1,
                            R.id.ip_property_tv_2,
                            R.id.ip_property_tv_3,
                            R.id.ip_property_tv_4,
                            R.id.ip_property_tv_5
                        )
                            .iterator()

                        with(viewData.ipAddress) {
                            // Prefix length
                            setTextView(
                                viewId = propertyTVIterator.next(),
                                text = "/$prefixLength",
                                color = widgetColors.secondary
                            )
                            if (isLocal) {
                                if (localAttributes.siteLocal) {
                                    setTextView(
                                        viewId = propertyTVIterator.next(),
                                        text = "SiteLocal",
                                        color = widgetColors.secondary
                                    )
                                }
                                if (localAttributes.linkLocal) {
                                    setTextView(
                                        viewId = propertyTVIterator.next(),
                                        text = "LinkLocal",
                                        color = widgetColors.secondary
                                    )
                                }
                            }
                            if (isLoopback) {
                                setTextView(
                                    viewId = propertyTVIterator.next(),
                                    text = "Loopback",
                                    color = widgetColors.secondary
                                )
                            }
                            if (isMultiCast) {
                                setTextView(
                                    viewId = propertyTVIterator.next(),
                                    text = "Multicast",
                                    color = widgetColors.secondary
                                )
                            }
                        }

                        propertyTVIterator.forEachRemaining {
                            setViewVisibility(it, View.GONE)
                        }
                    }
            }
        }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = nViewTypes

    override fun getItemId(position: Int): Long =
        propertyViewData[position].hashCode().toLong()

    override fun hasStableIds(): Boolean = true

    override fun onDestroy() {}
}