package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.w2sv.domain.model.WifiProperty

@Immutable
data class InfoDialogData(@param:StringRes val titleRes: Int, @param:StringRes val descriptionRes: Int, val learnMoreUrl: String? = null)

fun WifiProperty.infoDialogData(): InfoDialogData =
    InfoDialogData(
        titleRes = labelRes,
        descriptionRes = descriptionRes,
        learnMoreUrl = learnMoreUrl
    )
