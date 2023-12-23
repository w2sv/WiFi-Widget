package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import android.content.Context
import com.w2sv.domain.model.WidgetWifiProperty

data class PropertyInfoDialogData(
    val title: String,
    val description: String,
    val learnMoreUrl: String? = null,
)

fun WidgetWifiProperty.getInfoDialogData(context: Context): PropertyInfoDialogData =
    PropertyInfoDialogData(
        title = buildString {
            append(context.getString(labelRes))
            if (this@getInfoDialogData is WidgetWifiProperty.IP) {
                append(" Address")
            }
        },
        description = context.getString(descriptionRes),
        learnMoreUrl = learnMoreUrl,
    )
