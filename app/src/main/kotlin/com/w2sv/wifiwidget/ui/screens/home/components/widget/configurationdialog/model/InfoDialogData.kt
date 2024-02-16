package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.saveable.listSaver
import com.w2sv.domain.model.WidgetWifiProperty

@Immutable
data class InfoDialogData(
    val title: String,
    val description: String,
    val learnMoreUrl: String? = null,
) {
    companion object {
        val nullableStateSaver = listSaver<InfoDialogData?, String?>(
            save = {
                buildList {
                    it?.run {
                        add(title)
                        add(description)
                        add(learnMoreUrl)
                    }
                }
            },
            restore = {
                if (it.isEmpty()) {
                    null
                } else {
                    InfoDialogData(
                        title = it[0] as String,
                        description = it[1] as String,
                        learnMoreUrl = it[2]
                    )
                }
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
