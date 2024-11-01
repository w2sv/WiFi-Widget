package com.w2sv.widget.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.core.text.subscript
import com.w2sv.androidutils.appwidget.setBackgroundColor
import com.w2sv.common.utils.log
import com.w2sv.core.widget.R
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.WifiProperty
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.kotlinutils.coroutines.enabledKeys
import com.w2sv.widget.CopyPropertyToClipboardBroadcastReceiver
import com.w2sv.widget.data.appearanceBlocking
import com.w2sv.widget.model.WidgetColors
import com.w2sv.widget.utils.setTextView
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class WifiPropertyViewsFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetRepository: WidgetRepository,
    private val viewDataFactory: WifiProperty.ViewData.Factory,
) {
    @SuppressLint("NewApi")
    fun remoteCollectionItems(): RemoteViews.RemoteCollectionItems {
        val viewData: List<WifiProperty.ViewData> = runBlocking {
            viewDataFactory(
                properties = widgetRepository.wifiPropertyEnablementMap.enabledKeys(),
                ipSubProperties = widgetRepository.ipSubPropertyEnablementMap
                    .enabledKeys()
                    .toSet(),
            )
                .toList()
        }
            .log{ "viewData=$it" }

        val (widgetColors, fontSize) = widgetRepository.appearanceBlocking.let {
            it.getColors(context) to it.fontSize
        }

        return RemoteViews.RemoteCollectionItems.Builder()
            .apply {
                viewData.forEachIndexed { i, data ->
                    addItem(
                        data.hashCode().toLong(),
                        inflatePropertyLayout(
                            viewData = data,
                            packageName = context.packageName,
                            widgetColors = widgetColors,
                            fontSize = fontSize
                        )
                    )
                }
            }
            .setViewTypeCount(1)
            .setHasStableIds(true)
            .build()
    }
}

private fun inflatePropertyLayout(
    viewData: WifiProperty.ViewData,
    packageName: String,
    widgetColors: WidgetColors,
    fontSize: FontSize
): RemoteViews =
    RemoteViews(packageName, R.layout.wifi_property)
        .apply {
            setTextView(
                viewId = R.id.property_label_tv,
                text = if (viewData is WifiProperty.ViewData.NonIP)
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

            setOnClickFillInIntent(
                R.id.wifi_property_layout,
                CopyPropertyToClipboardBroadcastReceiver.Args.getIntent(
                    propertyLabel = viewData.label,
                    propertyValue = viewData.value
                )
            )

            viewData.ipPropertyOrNull?.prefixLengthText?.let { prefixLengthText ->
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
