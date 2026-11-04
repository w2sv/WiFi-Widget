package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.core.resources.R
import com.w2sv.domain.widget.WidgetColoring
import com.w2sv.domain.widget.WidgetColoringStrategy
import com.w2sv.wifiwidget.ui.designsystem.ColorIndicator
import com.w2sv.wifiwidget.ui.designsystem.ConfigureUseDynamicColors
import com.w2sv.wifiwidget.ui.designsystem.NestedSettingsTokens
import com.w2sv.wifiwidget.ui.designsystem.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigListTokens
import com.w2sv.wifiwidget.ui.designsystem.nestedContentBackground
import com.w2sv.wifiwidget.ui.designsystem.theme.WifiWidgetTheme
import com.w2sv.wifiwidget.ui.designsystem.theme.explanation
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.screen.widgetconfig.model.WidgetColor
import com.w2sv.wifiwidget.ui.screen.widgetconfig.model.get
import com.w2sv.wifiwidget.ui.screen.widgetconfig.model.labelRes

@Preview(showBackground = true)
@Composable
private fun PresetColoringPrev() {
    WifiWidgetTheme(useDarkTheme = false) {
        ConfigureColoring(
            WidgetColoring(),
            {},
            {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomColoringPrev() {
    WifiWidgetTheme(useDarkTheme = false) {
        ConfigureColoring(
            WidgetColoring(useCustom = true),
            {},
            {}
        )
    }
}

@Composable
fun ConfigureColoring(
    config: WidgetColoring,
    update: (WidgetColoring) -> Unit,
    showDialog: (WidgetConfigDialog) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        LabelContentRow(
            stringResource(R.string.color_scheme),
            modifier = Modifier.padding(bottom = ConfigListTokens.itemSpacing)
        ) {
            SelectColoringStrategyButtonRow(
                config = config,
                update = update,
                modifier = Modifier.fillMaxWidth()
            )
        }

        AnimatedContent(
            targetState = config.useCustom,
            modifier = Modifier
                .fillMaxWidth()
                .nestedContentBackground()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) { useCustomStrategy ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (useCustomStrategy) {
                    false -> {
                        ConfigurePresetColoring(
                            data = config.preset,
                            update = { update(config.copy(preset = it)) }
                        )
                    }

                    true -> {
                        ConfigureCustomColoring(
                            data = config.custom,
                            showDialog = showDialog
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectColoringStrategyButtonRow(
    config: WidgetColoring,
    update: (WidgetColoring) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier) {
        config.strategies.forEachIndexed { i, strategy ->
            SegmentedButton(
                selected = config.useCustom == strategy.isCustom,
                onClick = { update(config.copy(useCustom = strategy.isCustom)) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = i,
                    count = 2
                )
            ) {
                Text(text = stringResource(id = strategy.labelRes))
            }
        }
    }
}

@Composable
private fun ConfigurePresetColoring(
    data: WidgetColoringStrategy.Preset,
    update: (WidgetColoringStrategy.Preset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ThemeSelectionRow(
            modifier = Modifier.fillMaxWidth(),
            selected = data.theme,
            onSelected = { update(data.copy(theme = it)) },
            horizontalArrangement = Arrangement.SpaceBetween
        )
        if (dynamicColorsSupported) {
            ConfigureUseDynamicColors(
                useDynamicColors = data.useDynamicColors,
                toggleDynamicColors = { update(data.copy(useDynamicColors = it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ConfigListTokens.itemSpacing),
                below = {
                    AnimatedContent(data.useDynamicColors) { useDynamicColors ->
                        Text(
                            text = stringResource(
                                if (useDynamicColors) {
                                    R.string.use_colors_derived_from_your_wallpaper
                                } else {
                                    R.string.use_static_app_colors
                                }
                            ),
                            style = MaterialTheme.typography.explanation
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun ConfigureCustomColoring(
    data: WidgetColoringStrategy.Custom,
    showDialog: (WidgetConfigDialog) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ConfigListTokens.itemSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WidgetColor.entries.forEach { widgetColor ->
            val value = data[widgetColor]
            val label = stringResource(id = widgetColor.labelRes)
            CustomizationRow(
                label = label,
                color = Color(value),
                modifier = Modifier
                    .clickable(
                        onClickLabel = stringResource(
                            id = R.string.color_picker_button_cd,
                            label
                        ),
                        onClick = {
                            showDialog(
                                WidgetConfigDialog.ColorPicker(
                                    widgetColor = widgetColor,
                                    color = value,
                                    initialColor = value
                                )
                            )
                        }
                    )
            )
        }
    }
}

@Composable
private fun CustomizationRow(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = NestedSettingsTokens.fontSize,
            modifier = Modifier.weight(1f)
        )
        ColorIndicator(color)
    }
}
