package com.w2sv.wifiwidget.ui.screen.widgetconfig.list.appearance

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.core.common.R
import com.w2sv.domain.model.widget.WifiPropertyValueAlignment
import com.w2sv.domain.model.widget.FontSize
import com.w2sv.domain.model.widget.WidgetAppearance
import com.w2sv.domain.model.widget.WifiWidgetConfig
import com.w2sv.kotlinutils.enumEntryByOrdinal
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.designsystem.SliderWithLabel
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.screen.widgetconfig.list.WidgetConfigSectionCard
import com.w2sv.wifiwidget.ui.util.PreviewOf
import kotlin.math.roundToInt

object AppearanceConfigTokens {
    val featureSpacing = 12.dp
}

@Composable
fun AppearanceConfigCard(
    appearance: WidgetAppearance,
    updateAppearance: (WidgetAppearance.() -> WidgetAppearance) -> Unit,
    showDialog: (WidgetConfigDialog) -> Unit
) {
    WidgetConfigSectionCard(
        header = IconHeader(
            iconRes = R.drawable.ic_palette_24,
            stringRes = R.string.appearance
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            ConfigureColoring(
                config = appearance.coloring,
                update = { updateAppearance { copy(coloring = it) } },
                showDialog = showDialog,
                modifier = Modifier.padding(bottom = AppearanceConfigTokens.featureSpacing)
            )
            BackgroundOpacitySliderRow(
                opacity = appearance.backgroundOpacity,
                setOpacity = { updateAppearance { copy(backgroundOpacity = it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppearanceConfigTokens.featureSpacing)
            )
            FontSizeSliderRow(
                fontSize = appearance.fontSize,
                setFontSize = { updateAppearance { copy(fontSize = it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppearanceConfigTokens.featureSpacing)
            )
            ConfigurePropertyValueAlignment(
                alignment = appearance.propertyValueAlignment,
                update = { updateAppearance { copy(propertyValueAlignment = it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppearanceConfigTokens.featureSpacing)
            )
        }
    }
}

@Preview
@Composable
private fun Prev() {
    PreviewOf {
        AppearanceConfigCard(WifiWidgetConfig.default.appearance, {}, {})
    }
}

@Composable
private fun ConfigurePropertyValueAlignment(
    alignment: WifiPropertyValueAlignment,
    update: (WifiPropertyValueAlignment) -> Unit,
    modifier: Modifier = Modifier
) {
    LabelContentRow(
        label = stringResource(R.string.value_alignment),
        modifier = modifier
    ) {
        SingleChoiceSegmentedButtonRow {
            WifiPropertyValueAlignment.entries.forEach {
                SegmentedButton(
                    selected = it == alignment,
                    onClick = { update(it) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = it.ordinal,
                        count = 2
                    )
                ) {
                    Text(it.name)
                }
            }
        }
    }
}

@Composable
private fun FontSizeSliderRow(
    fontSize: FontSize,
    setFontSize: (FontSize) -> Unit,
    modifier: Modifier = Modifier
) {
    LabelContentRow(
        label = stringResource(id = R.string.font_size),
        modifier = modifier
    ) {
        val context: Context = LocalContext.current
        SliderWithLabel(
            value = fontSize.ordinal.toFloat(),
            steps = FontSize.entries.size - 2,
            makeLabel = { context.getString(enumEntryByOrdinal<FontSize>(it.roundToInt()).labelRes) },
            onValueChanged = { setFontSize(enumEntryByOrdinal(it.roundToInt())) },
            contentDescription = stringResource(id = R.string.font_size_slider_cd),
            valueRange = remember { 0f.rangeTo((FontSize.entries.size - 1).toFloat()) }
        )
    }
}

@Composable
private fun BackgroundOpacitySliderRow(
    opacity: Float,
    setOpacity: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    LabelContentRow(
        label = stringResource(R.string.background_opacity),
        modifier = modifier
    ) {
        SliderWithLabel(
            value = opacity,
            steps = 9,
            makeLabel = { "${(it * 100).roundToInt()}%" },
            onValueChanged = setOpacity,
            contentDescription = stringResource(id = R.string.opacity_slider_cd)
        )
    }
}

@Composable
private fun LabelContentRow(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(start = 12.dp)
    ) {
        Text(label, modifier = Modifier.weight(0.4f), maxLines = 2)
        Box(modifier = Modifier.weight(0.6f), contentAlignment = Alignment.Center) {
            content()
        }
    }
}
