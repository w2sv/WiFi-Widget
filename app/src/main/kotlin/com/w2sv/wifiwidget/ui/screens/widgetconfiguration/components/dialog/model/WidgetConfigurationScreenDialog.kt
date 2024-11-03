package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import com.w2sv.composed.nullableListSaver
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.CustomWidgetColor

@Immutable
sealed interface WidgetConfigurationScreenDialog {

    @Immutable
    @JvmInline
    value class Info(val data: InfoDialogData) : WidgetConfigurationScreenDialog {
        companion object {
            const val SAVER_LABEL = "Info"
        }
    }

    @Immutable
    @JvmInline
    value class ColorPicker(val data: ColorPickerDialogData) : WidgetConfigurationScreenDialog {
        companion object {
            const val SAVER_LABEL = "ColorPicker"
        }
    }

    @Immutable
    data object RefreshIntervalConfiguration : WidgetConfigurationScreenDialog {
        const val SAVER_LABEL = "RefreshIntervalConfiguration"
    }

    companion object {
        val nullableStateSaver: Saver<WidgetConfigurationScreenDialog?, Any> = nullableListSaver(
            saveNonNull = {
                when (it) {
                    is Info -> {
                        listOf(
                            Info.SAVER_LABEL,
                            it.data.title,
                            it.data.description,
                            it.data.learnMoreUrl
                        )
                    }

                    is ColorPicker -> {
                        listOf(
                            ColorPicker.SAVER_LABEL,
                            it.data.customWidgetColor,
                            it.data.appliedColor.toArgb(),
                            it.data.color.toArgb()
                        )
                    }

                    is RefreshIntervalConfiguration -> listOf(RefreshIntervalConfiguration.SAVER_LABEL)
                }
            },
            restoreNonNull = {
                when (it.first()) {
                    Info.SAVER_LABEL -> Info(
                        InfoDialogData(
                            title = it[1] as String,
                            description = it[2] as AnnotatedString,
                            learnMoreUrl = it[3] as String?
                        )
                    )

                    ColorPicker.SAVER_LABEL -> ColorPicker(
                        ColorPickerDialogData(
                            customWidgetColor = it[1] as CustomWidgetColor,
                            appliedColor = Color(it[2] as Int),
                            initialColor = Color(it[3] as Int)
                        )
                    )

                    RefreshIntervalConfiguration.SAVER_LABEL -> RefreshIntervalConfiguration

                    else -> throw IllegalArgumentException("Invalid WidgetConfigurationScreenDialog type label")
                }
            }
        )
    }
}