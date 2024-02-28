package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import android.content.Context
import androidx.compose.runtime.Immutable
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.ui.utils.nullableListSaver

@Immutable
data class InfoDialogData(
    val title: String,
    val description: String,
    val learnMoreUrl: String? = null,
) {
    companion object {
        val nullableStateSaver = nullableListSaver(
            saveNonNull = {
                listOf(it.title, it.description, it.learnMoreUrl)
            },
            restoreNonNull = {
                InfoDialogData(
                    title = it[0] as String,
                    description = it[1] as String,
                    learnMoreUrl = it[2]
                )
            }
        )
    }
}

fun WidgetWifiProperty.getInfoDialogData(context: Context): InfoDialogData =
    InfoDialogData(
        title = context.getString(labelRes),
        description = context.getString(descriptionRes),
        learnMoreUrl = learnMoreUrl,
    )
