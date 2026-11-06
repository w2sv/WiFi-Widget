package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.core.resources.R
import com.w2sv.domain.widget.FontSize
import com.w2sv.domain.widget.WidgetAppearance
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WifiPropertyValueAlignment
import com.w2sv.kotlinutils.enumEntryByOrdinal
import com.w2sv.wifiwidget.ui.designsystem.ElevatedCardWithHeader
import com.w2sv.wifiwidget.ui.designsystem.SliderWithLabel
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigListTokens
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.util.PreviewOf
import kotlin.math.roundToInt

private object AppearanceConfigCardToken {
    val paddingHorizontal = 16.dp
}

@Composable
fun AppearanceConfigCard(
    appearance: WidgetAppearance,
    updateAppearance: (WidgetAppearance.() -> WidgetAppearance) -> Unit,
    showDialog: (WidgetConfigDialog) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCardWithHeader(
        modifier = modifier.testTag("appearance_section"),
        iconRes = R.drawable.ic_palette_24,
        stringRes = R.string.appearance,
        innerPadding = WidgetConfigListDimens.sectionCardInnerPadding
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppearanceConfigCardToken.paddingHorizontal),
            verticalArrangement = Arrangement.spacedBy(ConfigListTokens.itemSpacing)
        ) {
            ConfigureColoring(
                config = appearance.coloring,
                update = { updateAppearance { copy(coloring = it) } },
                showDialog = showDialog
            )
            BackgroundOpacitySliderRow(
                opacity = appearance.backgroundOpacity,
                setOpacity = { updateAppearance { copy(backgroundOpacity = it) } },
                modifier = Modifier.fillMaxWidth()
            )
            FontSizeSliderRow(
                fontSize = appearance.fontSize,
                setFontSize = { updateAppearance { copy(fontSize = it) } },
                modifier = Modifier.fillMaxWidth()
            )
            ConfigurePropertyValueAlignment(
                alignment = appearance.propertyValueAlignment,
                update = { updateAppearance { copy(propertyValueAlignment = it) } },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
private fun Prev() {
    PreviewOf {
        AppearanceConfigCard(WidgetConfig.default.appearance, {}, {})
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
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            WifiPropertyValueAlignment.entries.forEach {
                SegmentedButton(
                    selected = it == alignment,
                    onClick = { update(it) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = it.ordinal,
                        count = WifiPropertyValueAlignment.entries.size
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
            contentDescription = stringResource(id = R.string.font_size),
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
            contentDescription = stringResource(id = R.string.background_opacity)
        )
    }
}

@Composable
fun LabelContentRow(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Text(label, modifier = Modifier.weight(0.4f), fontWeight = FontWeight.Medium)
        Box(modifier = Modifier.weight(0.6f), contentAlignment = Alignment.Center) {
            content()
        }
    }
}
