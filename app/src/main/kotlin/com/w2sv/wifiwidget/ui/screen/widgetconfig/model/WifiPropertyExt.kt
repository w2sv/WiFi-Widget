package com.w2sv.wifiwidget.ui.screen.widgetconfig.model

import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog

fun WifiProperty.infoDialogData(): WidgetConfigDialog.Info =
    WidgetConfigDialog.Info(
        titleRes = labelRes,
        descriptionRes = descriptionRes,
        learnMoreUrl = learnMoreUrl
    )
