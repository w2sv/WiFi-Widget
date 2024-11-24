package com.w2sv.widget.layout

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.core.text.subscript
import com.w2sv.androidutils.appwidget.setBackgroundColor
import com.w2sv.common.utils.log
import com.w2sv.core.widget.R
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.WifiProperty
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.kotlinutils.coroutines.flow.enabledKeys
import com.w2sv.widget.CopyPropertyToClipboardActivity
import com.w2sv.widget.data.appearanceBlocking
import com.w2sv.widget.model.WidgetColors
import com.w2sv.widget.utils.setTextView
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import slimber.log.i

internal class WifiPropertyViewsFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetRepository: WidgetRepository,
    private val viewDataFactory: WifiProperty.ViewData.Factory
) : RemoteViewsService.RemoteViewsFactory {

    override fun onCreate() {}

    private lateinit var viewData: List<WifiProperty.ViewData>
    private lateinit var widgetColors: WidgetColors
    private lateinit var fontSize: FontSize

    override fun onDataSetChanged() {
        i { "WifiPropertyViewsFactory.onDataSetChanged" }

        viewData = runBlocking {
            viewDataFactory(
                properties = widgetRepository.sortedEnabledWifiProperties.first(),
                ipSubProperties = widgetRepository.ipSubPropertyEnablementMap
                    .enabledKeys()
                    .toSet()
            )
                .toList()
        }
            .log { "Set propertyViewData=$it" }

        widgetRepository.appearanceBlocking.let {
            widgetColors = it.getColors(context)
            fontSize = it.fontSize
        }
    }

    override fun getCount(): Int =
        viewData.size

    override fun getViewAt(position: Int): RemoteViews =
        try {
            inflatePropertyLayout(
                viewData = viewData[position],
                packageName = context.packageName,
                widgetColors = widgetColors,
                fontSize = fontSize
            )
        } catch (e: IndexOutOfBoundsException) { // Fix irreproducible IndexOutOfBoundsException observed in play console
            e.log()
            RemoteViews(context.packageName, R.layout.wifi_property)
        }

    override fun getLoadingView(): RemoteViews =
        RemoteViews(context.packageName, R.layout.loading)
            .apply {
                setTextColor(R.id.loading_tv, widgetColors.secondary)
            }

    override fun getViewTypeCount(): Int =
        1

    override fun getItemId(position: Int): Long =
        try {
            viewData[position].hashCode().toLong()
        } catch (e: IndexOutOfBoundsException) { // Same as above
            i { e.toString() }
            -1L
        }

    override fun hasStableIds(): Boolean =
        true

    override fun onDestroy() {}
}

private fun inflatePropertyLayout(
    viewData: WifiProperty.ViewData,
    packageName: String,
    widgetColors: WidgetColors,
    fontSize: FontSize
): RemoteViews =
    RemoteViews(packageName, R.layout.wifi_property)
        .apply {
            i { "Inflating $viewData" }
            setTextView(
                viewId = R.id.property_label_tv,
                text = if (viewData is WifiProperty.ViewData.NonIP) {
                    viewData.label
                } else {
                    buildSpannedString {
                        append(IP_LABEL)
                        subscript {
                            scale(0.8f) {
                                append(viewData.label)
                            }
                        }
                    }
                },
                size = fontSize.value,
                color = widgetColors.primary
            )
            setTextView(
                viewId = R.id.property_value_tv,
                text = viewData.value,
                size = fontSize.value,
                color = widgetColors.secondary
            )

            setOnClickFillInIntent(
                R.id.wifi_property_layout,
                CopyPropertyToClipboardActivity.Args.getIntent(
                    propertyLabel = viewData.label,
                    propertyValue = viewData.value
                )
            )

            viewData.ipPropertyOrNull?.nonEmptySubPropertyValuesOrNull?.let { subPropertyValues ->
                setViewVisibility(R.id.ip_sub_property_row, View.VISIBLE)

                val subPropertyViewIterator = sequenceOf(R.id.ip_sub_property_tv_2, R.id.ip_sub_property_tv_1).iterator()
                subPropertyValues.reversed().forEach { value ->
                    val viewId = subPropertyViewIterator.next()
                    setTextView(
                        viewId = viewId,
                        text = value,
                        size = fontSize.subscriptSize,
                        color = widgetColors.secondary
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        setColorStateList(
                            viewId,
                            "setBackgroundTintList",
                            ColorStateList.valueOf(widgetColors.ipSubPropertyBackgroundColor)
                        )
                    } else {
                        setBackgroundColor(
                            viewId,
                            widgetColors.ipSubPropertyBackgroundColor
                        )
                    }
                }
                subPropertyViewIterator.forEachRemaining { setViewVisibility(it, View.GONE) }
            } ?: setViewVisibility(R.id.ip_sub_property_row, View.GONE)
        }

private const val IP_LABEL = "IP"
