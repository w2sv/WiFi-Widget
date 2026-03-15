package com.w2sv.wifiwidget.ui.screen.widgetconfig.list.appearance

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.core.common.R
import com.w2sv.domain.model.widget.WidgetColoring
import com.w2sv.domain.model.widget.WidgetColoringStrategy
import com.w2sv.wifiwidget.ui.designsystem.SecondLevelElevatedCard
import com.w2sv.wifiwidget.ui.designsystem.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.designsystem.UseDynamicColorsRow
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.screen.widgetconfig.model.WidgetColor
import com.w2sv.wifiwidget.ui.screen.widgetconfig.model.get
import com.w2sv.wifiwidget.ui.screen.widgetconfig.model.labelRes
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.theme.onSurfaceVariantLowAlpha

@Composable
fun ConfigureColoring(
    config: WidgetColoring,
    update: (WidgetColoring) -> Unit,
    showDialog: (WidgetConfigDialog) -> Unit,
    modifier: Modifier = Modifier
) {
    SecondLevelElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SelectColoringStrategyButtonRow(
                config = config,
                update = update,
                modifier = Modifier.padding(bottom = AppearanceConfigTokens.featureSpacing)
            )

            AnimatedContent(targetState = config.useCustom) { useCustomStrategy ->
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    when (useCustomStrategy) {
                        false -> {
                            PresetColoringConfiguration(
                                data = config.preset,
                                update = { update(config.copy(preset = it)) },
                                modifier = Modifier.fillMaxWidth(0.82f)
                            )
                        }

                        true -> {
                            CustomColorConfiguration(
                                data = config.custom,
                                showDialog = showDialog,
                                modifier = Modifier.fillMaxWidth(0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PresetColoringPrev() {
    AppTheme(useDarkTheme = false) {
        ConfigureColoring(
            WidgetColoring(),
            {},
            {}
        )
    }
}

@Preview
@Composable
private fun CustomColoringPrev() {
    AppTheme(useDarkTheme = false) {
        ConfigureColoring(
            WidgetColoring(useCustom = true),
            {},
            {}
        )
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
private fun PresetColoringConfiguration(
    data: WidgetColoringStrategy.Preset,
    update: (WidgetColoringStrategy.Preset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ThemeSelectionRow(
            modifier = Modifier.fillMaxWidth(),
            selected = data.theme,
            onSelected = { update(data.copy(theme = it)) },
            horizontalArrangement = Arrangement.SpaceEvenly
        )
        if (dynamicColorsSupported) {
            val startPadding = 20.dp
            UseDynamicColorsRow(
                useDynamicColors = data.useDynamicColors,
                toggleDynamicColors = { update(data.copy(useDynamicColors = it)) },
                modifier = Modifier.padding(top = AppearanceConfigTokens.featureSpacing, start = startPadding)
            )
            AnimatedContent(data.useDynamicColors) { useDynamicColors ->
                Text(
                    text = stringResource(
                        if (useDynamicColors) {
                            R.string.use_colors_derived_from_your_wallpaper
                        } else {
                            R.string.use_static_app_colors
                        }
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariantLowAlpha,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(start = startPadding, end = 52.dp) // Align with start of dynamic colors label and start of the switch
                        .offset(y = (-8).dp) // Move up as a workaround for the stupid built-in switch bottom padding
                )
            }
        }
    }
}

@Composable
private fun CustomColorConfiguration(
    data: WidgetColoringStrategy.Custom,
    showDialog: (WidgetConfigDialog) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color)
                .border(0.5.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
        )
    }
}
