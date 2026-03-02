package com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import com.w2sv.domain.model.widget.WidgetColoring
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.wifiwidget.ui.screen.widgetconfig.list.UpdateWidgetConfig
import com.w2sv.wifiwidget.ui.screen.widgetconfig.model.WidgetColor
import kotlinx.parcelize.Parcelize
import kotlin.time.Duration

@Parcelize
sealed interface WidgetConfigDialog : Parcelable {

    @Parcelize
    data class Info(
        @StringRes val titleRes: Int,
        @StringRes val descriptionRes: Int,
        val learnMoreUrl: String? = null
    ) : WidgetConfigDialog

    @Parcelize
    data class ColorPicker(
        val widgetColor: WidgetColor,
        val color: Int,
        private val initialColor: Int
    ) : WidgetConfigDialog {
        val hasBeenConfigured: Boolean
            get() = initialColor != color
    }

    @Parcelize
    @JvmInline
    value class RefreshIntervalPicker(val interval: Duration) : WidgetConfigDialog
}

@Composable
fun WidgetConfigDialog(
    dialog: WidgetConfigDialog,
    updateDialog: (WidgetConfigDialog) -> Unit,
    updateConfig: UpdateWidgetConfig,
    onDismissRequest: () -> Unit
) {
    when (dialog) {
        is WidgetConfigDialog.Info -> {
            PropertyInfoDialog(
                data = dialog,
                onDismissRequest = onDismissRequest
            )
        }

        is WidgetConfigDialog.ColorPicker -> {
            ColorPickerDialog(
                data = dialog,
                updateColor = { updateDialog(dialog.copy(color = it.toArgb())) },
                applyColor = { color ->
                    updateConfig {
                        copy(
                            appearance = appearance.copy(
                                coloringConfig = appearance.coloringConfig.copy(
                                    custom = appearance.coloringConfig.custom.withColor(dialog.widgetColor, color)
                                )
                            )
                        )
                    }
                },
                onDismissRequest = onDismissRequest
            )
        }

        is WidgetConfigDialog.RefreshIntervalPicker -> {
            RefreshIntervalConfigurationDialog(
                interval = dialog.interval,
                setInterval = { updateConfig { copy(refreshing = refreshing.copy(interval = it)) } },
                onDismissRequest = onDismissRequest
            )
        }
    }
}

private fun WidgetColoring.Custom.withColor(color: WidgetColor, @ColorInt value: Int): WidgetColoring.Custom =
    when (color) {
        WidgetColor.Background -> copy(background = value)
        WidgetColor.Primary -> copy(primary = value)
        WidgetColor.Secondary -> copy(secondary = value)
    }

fun WifiProperty.infoDialogData(): WidgetConfigDialog.Info =
    WidgetConfigDialog.Info(
        titleRes = labelRes,
        descriptionRes = descriptionRes,
        learnMoreUrl = learnMoreUrl
    )
