package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import androidx.annotation.StringRes
import com.w2sv.domain.model.WidgetWifiProperty

data class PropertyInfoDialogData(
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int,
    val learnMoreUrl: String? = null,
)

val WidgetWifiProperty.ViewData.infoDialogData: PropertyInfoDialogData
    get() = PropertyInfoDialogData(
        labelRes,
        descriptionRes,
        learnMoreUrl,
    )
