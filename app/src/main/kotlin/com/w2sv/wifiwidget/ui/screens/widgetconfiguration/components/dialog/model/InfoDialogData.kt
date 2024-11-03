package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.w2sv.domain.model.WifiProperty

@Immutable
data class InfoDialogData(
    @StringRes val title: Int,
    @StringRes val description: Int,
    val learnMoreUrl: String? = null,
)

fun WifiProperty.infoDialogData(): InfoDialogData =
    InfoDialogData(
        title = labelRes,
        description = descriptionRes,
        learnMoreUrl = learnMoreUrl,
    )
