package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable

@Stable
data class PropertyInfoDialogData(
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int,
    val learnMoreUrl: String? = null
)