package com.w2sv.wifiwidget.ui.screen.widgetconfig.list.appearance

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.sp
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.domain.model.widget.WidgetColoring
import com.w2sv.domain.model.widget.WidgetColoringStrategy
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.KeyboardArrowRightIcon
import com.w2sv.wifiwidget.ui.designsystem.SecondLevelElevatedCard
import com.w2sv.wifiwidget.ui.designsystem.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.designsystem.UseDynamicColorsRow
import com.w2sv.wifiwidget.ui.designsystem.colorButton
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.screen.widgetconfig.model.WidgetColor
import com.w2sv.wifiwidget.ui.screen.widgetconfig.model.get
import com.w2sv.wifiwidget.ui.screen.widgetconfig.model.labelRes
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.theme.onSurfaceVariantLowAlpha
import com.w2sv.wifiwidget.ui.util.contentDescription

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

            AnimatedContent(targetState = config.appliedStrategy) { strategy ->
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    when (strategy) {
                        is WidgetColoringStrategy.Preset -> {
                            PresetColoringConfiguration(
                                data = strategy,
                                update = { update(config.copy(preset = it)) },
                                modifier = Modifier.fillMaxWidth(0.82f)
                            )
                        }

                        is WidgetColoringStrategy.Custom -> {
                            CustomColorConfiguration(
                                data = strategy,
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
            UseDynamicColorsRow(
                useDynamicColors = data.useDynamicColors,
                toggleDynamicColors = { update(data.copy(useDynamicColors = it)) },
                modifier = Modifier.padding(top = AppearanceConfigTokens.featureSpacing),
                leadingIcon = { KeyboardArrowRightIcon(modifier = Modifier.padding(end = 8.dp)) }
            )
            Text(
                stringResource(R.string.use_colors_derived_from_your_wallpaper),
                color = MaterialTheme.colorScheme.onSurfaceVariantLowAlpha,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 32.dp)
            )
        }
    }
}

@Composable
private fun CustomColorConfiguration(
    data: WidgetColoringStrategy.Custom,
    showDialog: (WidgetConfigDialog) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        WidgetColor.entries.forEach { widgetColor ->
            val value = data[widgetColor]
            SectionCustomizationRow(
                label = stringResource(id = widgetColor.labelRes),
                color = Color(value),
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
        }
    }
}

@Composable
private fun SectionCustomizationRow(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        KeyboardArrowRightIcon()
        Text(
            text = label,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        val colorPickerButtonCD = stringResource(
            id = R.string.color_picker_button_cd,
            label
        )
        Button(
            modifier = Modifier
                .colorButton()
                .contentDescription(colorPickerButtonCD),
            colors = ButtonDefaults.buttonColors(containerColor = color),
            onClick = onClick,
            shape = CircleShape,
            content = {}
        )
    }
}
