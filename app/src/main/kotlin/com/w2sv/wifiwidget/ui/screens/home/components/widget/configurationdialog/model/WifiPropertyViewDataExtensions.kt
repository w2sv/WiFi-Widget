package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import com.w2sv.data.model.WifiProperty

val WidgetWifiProperty.ViewData.infoDialogData: PropertyInfoDialogData
    get() = PropertyInfoDialogData(
        labelRes,
        descriptionRes,
        learnMoreUrl,
    )
