package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.w2sv.composed.getAnnotatedString
import com.w2sv.domain.model.WifiProperty

@Immutable
data class InfoDialogData(
    val title: String,
    val description: AnnotatedString,
    val learnMoreUrl: String? = null,
)

fun WifiProperty.infoDialogData(context: Context): InfoDialogData =
    InfoDialogData(
        title = context.getString(labelRes),
        description = context.resources.getAnnotatedString(descriptionRes),
        learnMoreUrl = learnMoreUrl,
    )
