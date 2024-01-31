package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import android.content.Context
import androidx.compose.runtime.Immutable
import com.w2sv.domain.model.WidgetWifiProperty

@Immutable
data class InfoDialogData(
    val title: String,
    val description: String,
    val learnMoreUrl: String? = null,
)

fun WidgetWifiProperty.getInfoDialogData(context: Context): InfoDialogData =
    InfoDialogData(
        title = buildString {
            append(context.getString(labelRes))
            if (this@getInfoDialogData is WidgetWifiProperty.IP) {
                append(" Address")
            }
        },
        description = context.getString(descriptionRes),
        learnMoreUrl = learnMoreUrl,
    )
