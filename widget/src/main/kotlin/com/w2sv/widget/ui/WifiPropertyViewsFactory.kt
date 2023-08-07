package com.w2sv.widget.ui

import android.content.Context
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.data.model.WifiProperty
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
    @ApplicationContext private val context: Context, private val widgetRepository: WidgetRepository
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
                            WifiProperty.IPv4 -> value.addresses
                            WifiProperty.IPv6 -> value.addresses.filter { address ->
                                ipProperty as WifiProperty.IPv6

                                when {
                                    !ipSubProperties.getValue(ipProperty.local) -> !address.isLocal
                                    !ipSubProperties.getValue(ipProperty.public) -> address.isLocal
                                    else -> true
                                }
                            }
                        }
                        filteredAddresses.forEachIndexed { i, address ->
                            add(
                                WifiPropertyLayoutViewData.WifiProperty(
                                    context.getString(it.viewData.labelRes)
                                        .run {
                                            if (filteredAddresses.size > 1)
                                                "$this #${i + 1}"
                                            else
                                                this
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
        when (val viewData = propertyViewData[position]) {
            is WifiPropertyLayoutViewData.WifiProperty -> {
                RemoteViews(context.packageName, R.layout.wifi_property).apply {
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

                        fun addSubPropertyTV(text: String) {
                            setTextView(
                                viewId = propertyTVIterator.next(),
                                text = text,
                                color = widgetColors.secondary
                            )
                        }

                        fun addSubPropertyTVIfConditionMet(conditional: Boolean, text: String) {
                            if (conditional) {
                                addSubPropertyTV(text)
                            }
                        }

                        // Prefix length
                        addSubPropertyTVIfConditionMet(
                            viewData.showPrefixLength, "/${viewData.ipAddress.prefixLength}"
                        )

                        // Local / Public
                        if (viewData.ipAddress.isLocal) {
                            addSubPropertyTVIfConditionMet(
                                viewData.ipAddress.localAttributes.siteLocal, "SiteLocal"
                            )
                            addSubPropertyTVIfConditionMet(
                                viewData.ipAddress.localAttributes.linkLocal, "LinkLocal"
                            )
                            addSubPropertyTVIfConditionMet(
                                !viewData.ipAddress.localAttributes.siteLocal && !viewData.ipAddress.localAttributes.linkLocal,
                                "Local"
                            )
                        } else {
                            addSubPropertyTV("Public")
                        }

                        // Additional properties
                        addSubPropertyTVIfConditionMet(viewData.ipAddress.isLoopback, "Loopback")
                        addSubPropertyTVIfConditionMet(viewData.ipAddress.isMultiCast, "Multicast")

                        // Hide all remaining sub-property views
                        propertyTVIterator.forEachRemaining {
                            setViewVisibility(it, View.GONE)
                        }
                    }
            }
        }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = nViewTypes

    override fun getItemId(position: Int): Long = propertyViewData[position].hashCode().toLong()

    override fun hasStableIds(): Boolean = true

    override fun onDestroy() {}
}