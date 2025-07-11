package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.KeyboardArrowRightIcon
import com.w2sv.wifiwidget.ui.designsystem.SecondLevelElevatedCard
import com.w2sv.wifiwidget.ui.designsystem.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.designsystem.UseDynamicColorsRow
import com.w2sv.wifiwidget.ui.designsystem.colorButton
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.ColorPickerDialogData
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.CustomWidgetColor
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.theme.onSurfaceVariantLowAlpha

@Composable
fun ColoringConfiguration(
    coloringConfig: WidgetColoring.Config,
    setColoringConfig: (WidgetColoring.Config) -> Unit,
    showCustomColorConfigurationDialog: (ColorPickerDialogData) -> Unit,
    modifier: Modifier = Modifier
) {
    SecondLevelElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ColoringStyleSelectionButtons(
                coloringConfig = coloringConfig,
                setColoringConfig = setColoringConfig,
                modifier = Modifier
                    .padding(bottom = AppearanceConfigurationDefaults.verticalPadding)
            )

            AnimatedContent(
                targetState = coloringConfig.isCustomSelected,
                label = ""
            ) { isCustomStyleSelected ->
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    when (isCustomStyleSelected) {
                        false -> {
                            PresetColoringConfiguration(
                                data = coloringConfig.preset,
                                setData = { setColoringConfig(coloringConfig.copy(preset = it)) },
                                modifier = Modifier.fillMaxWidth(0.82f)
                            )
                        }

                        true -> {
                            CustomColorConfiguration(
                                data = coloringConfig.custom,
                                showCustomColorConfigurationDialog = showCustomColorConfigurationDialog,
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
        ColoringConfiguration(
            WidgetColoring.Config(),
            {},
            {}
        )
    }
}

@Preview
@Composable
private fun CustomColoringPrev() {
    AppTheme(useDarkTheme = false) {
        ColoringConfiguration(
            WidgetColoring.Config(isCustomSelected = true),
            {},
            {}
        )
    }
}

@Composable
private fun ColoringStyleSelectionButtons(
    coloringConfig: WidgetColoring.Config,
    setColoringConfig: (WidgetColoring.Config) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier) {
        coloringConfig.styles.forEachIndexed { i, style ->
            val isSelected = remember(coloringConfig.isCustomSelected) {
                style::class == coloringConfig.appliedStyle::class
            }
            SegmentedButton(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        setColoringConfig(coloringConfig.copy(isCustomSelected = style is WidgetColoring.Style.Custom))
                    }
                },
                shape = SegmentedButtonDefaults.itemShape(
                    index = i,
                    count = 2
                )
            ) {
                Text(text = stringResource(id = style.labelRes))
            }
        }
    }
}

@Composable
private fun PresetColoringConfiguration(
    data: WidgetColoring.Style.Preset,
    setData: (WidgetColoring.Style.Preset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ThemeSelectionRow(
            modifier = Modifier.fillMaxWidth(),
            selected = data.theme,
            onSelected = { setData(data.copy(theme = it)) },
            horizontalArrangement = Arrangement.SpaceEvenly
        )
        if (dynamicColorsSupported) {
            UseDynamicColorsRow(
                useDynamicColors = data.useDynamicColors,
                toggleDynamicColors = { setData(data.copy(useDynamicColors = it)) },
                modifier = Modifier.padding(top = AppearanceConfigurationDefaults.verticalPadding),
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
    data: WidgetColoring.Style.Custom,
    showCustomColorConfigurationDialog: (ColorPickerDialogData) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CustomWidgetColor.entries
            .forEach { widgetColorType ->
                val color = remember(widgetColorType, data) {
                    widgetColorType.getColor(data)
                }
                SectionCustomizationRow(
                    label = stringResource(id = widgetColorType.labelRes),
                    color = color,
                    onClick = {
                        showCustomColorConfigurationDialog(
                            ColorPickerDialogData(
                                customWidgetColor = widgetColorType,
                                appliedColor = color
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
        val colorPickerButtonCD =
            stringResource(
                id = R.string.color_picker_button_cd,
                label
            )
        Button(
            modifier = Modifier
                .colorButton()
                .semantics { contentDescription = colorPickerButtonCD },
            colors = ButtonDefaults.buttonColors(
                containerColor = color
            ),
            onClick = onClick,
            shape = CircleShape,
            content = {}
        )
    }
}
