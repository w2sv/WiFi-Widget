package com.w2sv.widget.ui

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.core.text.subscript
import com.w2sv.androidutils.appwidgets.setBackgroundColor
import com.w2sv.common.utils.enabledKeys
import com.w2sv.core.widget.R
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.widget.data.appearance
import com.w2sv.widget.model.WidgetColors
import com.w2sv.widget.utils.setTextView
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import slimber.log.i
import javax.inject.Inject

class WifiPropertyViewsFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetRepository: WidgetRepository,
    private val viewDataFactory: WidgetWifiProperty.ViewData.Factory,
) : RemoteViewsService.RemoteViewsFactory {

    override fun onCreate() {}

    private lateinit var viewData: List<WidgetWifiProperty.ViewData>
    private lateinit var widgetColors: WidgetColors
    private lateinit var fontSize: FontSize

    override fun onDataSetChanged() {
        i { "WifiPropertyViewsFactory.onDataSetChanged" }

        viewData = runBlocking {
            viewDataFactory(
                properties = widgetRepository.wifiPropertyEnablementMap.enabledKeys(),
                ipSubProperties = widgetRepository.ipSubPropertyEnablementMap
                    .enabledKeys()
                    .toSet(),
            )
                .toList()
        }
            .also { i { "Set propertyViewData=$it" } }

        widgetRepository.appearance.let {
            widgetColors = it.getColors(context)
            fontSize = it.fontSize
        }
    }

    override fun getCount(): Int = viewData.size

    override fun getViewAt(position: Int): RemoteViews =
        try {
            inflatePropertyLayout(
                viewData = viewData[position],
                packageName = context.packageName,
                widgetColors = widgetColors,
                fontSize = fontSize
            )
        } catch (e: IndexOutOfBoundsException) {  // Fix irreproducible IndexOutOfBoundsException observed in play console
            i { e.toString() }
            RemoteViews(context.packageName, R.layout.wifi_property)
        }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long =
        try {
            viewData[position].hashCode().toLong()
        } catch (e: IndexOutOfBoundsException) {  // Same as above
            i { e.toString() }
            -1L
        }

    override fun hasStableIds(): Boolean = true

    override fun onDestroy() {}
}

private fun inflatePropertyLayout(
    viewData: WidgetWifiProperty.ViewData,
    packageName: String,
    widgetColors: WidgetColors,
    fontSize: FontSize
): RemoteViews =
    RemoteViews(packageName, R.layout.wifi_property)
        .apply {
            setTextView(
                viewId = R.id.property_label_tv,
                text = if (viewData is WidgetWifiProperty.ViewData.NonIP)
                    viewData.label
                else
                    buildSpannedString {
                        append(IP_LABEL)
                        subscript {
                            scale(0.8f) {
                                append(viewData.label)
                            }
                        }
                    },
                size = fontSize.value,
                color = widgetColors.primary,
            )
            setTextView(
                viewId = R.id.property_value_tv,
                text = viewData.value,
                size = fontSize.value,
                color = widgetColors.secondary,
            )

            (viewData as? WidgetWifiProperty.ViewData.IPProperty)?.prefixLengthText?.let { prefixLengthText ->
                setViewVisibility(R.id.prefix_length_row, View.VISIBLE)

                setTextView(
                    viewId = R.id.prefix_length_tv,
                    text = prefixLengthText,
                    size = fontSize.subscriptSize,
                    color = widgetColors.secondary,
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setColorStateList(
                        R.id.prefix_length_tv,
                        "setBackgroundTintList",
                        ColorStateList.valueOf(widgetColors.ipSubPropertyBackgroundColor),
                    )
                } else {
                    setBackgroundColor(
                        R.id.prefix_length_tv,
                        widgetColors.ipSubPropertyBackgroundColor
                    )
                }
            } ?: setViewVisibility(R.id.prefix_length_row, View.GONE)
        }

private const val IP_LABEL = "IP"
