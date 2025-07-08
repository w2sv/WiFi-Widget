package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration

import android.content.Context
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.PropertyValueAlignment
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.kotlinutils.enumEntryByOrdinal
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.ArrowRightLabelContentRow
import com.w2sv.wifiwidget.ui.designsystem.CardContainerColor
import com.w2sv.wifiwidget.ui.designsystem.KeyboardArrowRightIcon
import com.w2sv.wifiwidget.ui.designsystem.SliderWithLabel
import com.w2sv.wifiwidget.ui.designsystem.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.designsystem.UseDynamicColorsRow
import com.w2sv.wifiwidget.ui.designsystem.colorButton
import com.w2sv.wifiwidget.ui.designsystem.nestedContentBackground
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.ColorPickerDialogData
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.CustomWidgetColor
import kotlin.math.roundToInt

private val verticalPadding = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceConfiguration(
    coloringConfig: WidgetColoring.Config,
    setColoringConfig: (WidgetColoring.Config) -> Unit,
    opacity: Float,
    setOpacity: (Float) -> Unit,
    fontSize: FontSize,
    setFontSize: (FontSize) -> Unit,
    propertyValueAlignment: PropertyValueAlignment,
    setPropertyValueAlignment: (PropertyValueAlignment) -> Unit,
    showCustomColorConfigurationDialog: (ColorPickerDialogData) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ColoringConfiguration(
            coloringConfig = coloringConfig,
            setColoringConfig = setColoringConfig,
            showCustomColorConfigurationDialog = showCustomColorConfigurationDialog,
            modifier = Modifier
                .padding(bottom = verticalPadding)
                .align(Alignment.CenterHorizontally)
        )
        BackgroundOpacitySliderRow(
            opacity = opacity,
            setOpacity = setOpacity,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = verticalPadding)
        )
        FontSizeSliderRow(
            fontSize = fontSize,
            setFontSize = setFontSize,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = verticalPadding)
        )
        ArrowRightLabelContentRow(
            stringResource(R.string.value_alignment),
            content = {
                SingleChoiceSegmentedButtonRow {
                    PropertyValueAlignment.entries.forEach {
                        SegmentedButton(
                            selected = it == propertyValueAlignment,
                            onClick = { setPropertyValueAlignment(it) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = it.ordinal,
                                count = 2
                            )
                        ) {
                            Text(it.name)
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = verticalPadding)
        )
    }
}

@Composable
private fun FontSizeSliderRow(
    fontSize: FontSize,
    setFontSize: (FontSize) -> Unit,
    modifier: Modifier = Modifier
) {
    val context: Context = LocalContext.current
    ArrowRightLabelContentRow(
        label = stringResource(id = R.string.font_size),
        content = {
            SliderWithLabel(
                value = fontSize.ordinal.toFloat(),
                steps = remember { FontSize.entries.size - 2 },
                makeLabel = remember { { context.getString(enumEntryByOrdinal<FontSize>(it.roundToInt()).labelRes) } },
                onValueChanged = remember { { setFontSize(enumEntryByOrdinal(it.roundToInt())) } },
                contentDescription = stringResource(id = R.string.font_size_slider_cd),
                valueRange = remember { 0f.rangeTo((FontSize.entries.size - 1).toFloat()) }
            )
        },
        modifier = modifier
    )
}

@Composable
private fun BackgroundOpacitySliderRow(
    opacity: Float,
    setOpacity: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    ArrowRightLabelContentRow(
        label = stringResource(R.string.background_opacity),
        content = {
            SliderWithLabel(
                value = opacity,
                steps = 9,
                makeLabel = remember { { "${(it * 100).roundToInt()}%" } },
                onValueChanged = setOpacity,
                contentDescription = stringResource(id = R.string.opacity_slider_cd)
            )
        },
        modifier = modifier
    )
}

@Composable
private fun ColoringConfiguration(
    coloringConfig: WidgetColoring.Config,
    setColoringConfig: (WidgetColoring.Config) -> Unit,
    showCustomColorConfigurationDialog: (ColorPickerDialogData) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        coloringConfig.styles.forEachIndexed { i, style ->
            SegmentedButton(
                selected = style.javaClass == coloringConfig.appliedStyle.javaClass,
                onClick = {
                    if (style.javaClass != coloringConfig.appliedStyle.javaClass) {
                        setColoringConfig(coloringConfig.copy(isCustomSelected = style is WidgetColoring.Style.Custom))
                    }
                },
                colors = SegmentedButtonDefaults.colors(
                    inactiveContainerColor = CardContainerColor
                ),
                shape = SegmentedButtonDefaults.itemShape(
                    index = i,
                    count = 2
                )
            ) {
                Text(text = stringResource(id = style.labelRes))
            }
        }
    }

    val styleConfigurationModifier =
        Modifier
            .nestedContentBackground()
            .padding(horizontal = 16.dp, vertical = 12.dp)

    AnimatedContent(
        targetState = coloringConfig.isCustomSelected,
        label = ""
    ) { isCustomStyleSelected ->
        when (isCustomStyleSelected) {
            false -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    PresetColoringConfiguration(
                        data = coloringConfig.preset,
                        setData = remember { { setColoringConfig(coloringConfig.copy(preset = it)) } },
                        modifier = styleConfigurationModifier.fillMaxWidth(0.9f)
                    )
                }
            }

            true -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CustomColorConfiguration(
                        data = coloringConfig.custom,
                        showCustomColorConfigurationDialog = showCustomColorConfigurationDialog,
                        modifier = styleConfigurationModifier.fillMaxWidth(0.7f)
                    )
                }
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
            modifier = Modifier
                .fillMaxWidth(),
            selected = data.theme,
            onSelected = { setData(data.copy(theme = it)) },
            horizontalArrangement = Arrangement.SpaceEvenly
        )
        if (dynamicColorsSupported) {
            UseDynamicColorsRow(
                useDynamicColors = data.useDynamicColors,
                onToggleDynamicColors = {
                    setData(data.copy(useDynamicColors = it))
                },
                modifier = Modifier
                    .padding(top = verticalPadding),
                leadingIcon = {
                    KeyboardArrowRightIcon(modifier = Modifier.padding(end = 8.dp))
                }
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
