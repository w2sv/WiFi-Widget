package com.w2sv.widget.ui

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.data.model.WifiProperty
import com.w2sv.data.storage.WidgetRepository
import com.w2sv.widget.R
import com.w2sv.widget.data.appearance
import com.w2sv.widget.model.WidgetColors
import com.w2sv.widget.model.WifiPropertyView
import com.w2sv.widget.utils.setTextView
import dagger.hilt.android.qualifiers.ApplicationContext
import slimber.log.i
import javax.inject.Inject

class WifiPropertyViewsFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetRepository: WidgetRepository
) : RemoteViewsService.RemoteViewsFactory {

    private val valueGetterResources by lazy {
        WifiProperty.ValueGetterResources(context)
    }

    override fun onCreate() {}

    private lateinit var propertyViewData: List<WifiPropertyView>
    private lateinit var widgetColors: WidgetColors

    override fun onDataSetChanged() {
        i { "${this::class.simpleName}.onDataSetChanged" }

        propertyViewData = widgetRepository.getSetWifiProperties()
            .flatMap {
                when (val value = it.getValue(valueGetterResources)) {
                    is WifiProperty.Value.Singular -> {
                        listOf(
                            WifiPropertyView(
                                context.getString(it.viewData.labelRes),
                                value.value
                            )
                        )
                    }

                    is WifiProperty.Value.IPAddresses -> {
                        value.addresses.mapIndexed { i, address ->
                            WifiPropertyView(
                                "${context.getString(it.viewData.labelRes)} ${i + 1}",
                                address.textualRepresentation
                            )
                        }
                    }
                }
            }

        widgetColors = widgetRepository.appearance.getValueSynchronously().theme.getColors(context)
    }

    override fun getCount(): Int = propertyViewData.size

    override fun getViewAt(position: Int): RemoteViews =
        RemoteViews(context.packageName, R.layout.wifi_property)
            .apply {
                setTextView(
                    viewId = R.id.property_label_tv,
                    text = propertyViewData[position].label,
                    color = widgetColors.primary
                )
                setTextView(
                    viewId = R.id.property_value_tv,
                    text = propertyViewData[position].value,
                    color = widgetColors.secondary
                )
            }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long =
        propertyViewData[position].hashCode().toLong()

    override fun hasStableIds(): Boolean = true

    override fun onDestroy() {}
}