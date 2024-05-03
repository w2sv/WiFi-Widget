package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.w2sv.androidutils.generic.dynamicColorsSupported
import com.w2sv.common.utils.bulletPointText
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.kotlinutils.extensions.getByOrdinal
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.SliderRow
import com.w2sv.wifiwidget.ui.designsystem.SliderWithLabel
import com.w2sv.wifiwidget.ui.designsystem.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.designsystem.UseDynamicColorsRow
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
    showCustomColorConfigurationDialog: (ColorPickerProperties) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .padding(bottom = verticalPadding)
                .align(Alignment.CenterHorizontally)
        ) {
            coloringConfig.styles.forEachIndexed { i, style ->
                SegmentedButton(
                    selected = style.javaClass == coloringConfig.appliedStyle.javaClass,
                    onClick = remember(i) {
                        { setColoringConfig(coloringConfig.copy(isCustomSelected = style is WidgetColoring.Style.Custom)) }
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

        AnimatedContent(
            targetState = coloringConfig.isCustomSelected,
            label = "",
        ) { isCustomStyleSelected ->
            when (isCustomStyleSelected) {
                false -> {
                    PresetColoringConfiguration(
                        data = coloringConfig.preset,
                        setData = remember { { setColoringConfig(coloringConfig.copy(preset = it)) } }
                    )
                }

                true -> {
                    CustomColorConfiguration(
                        data = coloringConfig.custom,
                        showCustomColorConfigurationDialog = showCustomColorConfigurationDialog
                    )
                }
            }
        }

        SliderRow(
            label = stringResource(R.string.background_opacity),
            slider = {
                SliderWithLabel(
                    value = opacity,
                    steps = 9,
                    makeLabel = remember { { "${(it * 100).roundToInt()}%" } },
                    onValueChanged = setOpacity,
                    contentDescription = stringResource(id = R.string.opacity_slider_cd)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = verticalPadding)
        )

        val context: Context = LocalContext.current
        SliderRow(
            label = stringResource(id = R.string.font_size),
            slider = {
                SliderWithLabel(
                    value = fontSize.ordinal.toFloat(),
                    steps = remember { FontSize.entries.size - 2 },
                    makeLabel = remember { { context.getString(getByOrdinal<FontSize>(it.roundToInt()).labelRes) } },
                    onValueChanged = remember { { setFontSize(getByOrdinal(it.roundToInt())) } },
                    contentDescription = stringResource(id = R.string.font_size_slider_cd),
                    valueRange = remember { 0f.rangeTo((FontSize.entries.size - 1).toFloat()) }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
        )
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
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
            )
        }
    }
}

enum class CustomWidgetColor(@StringRes val labelRes: Int) {
    Background(com.w2sv.core.domain.R.string.background),
    Primary(com.w2sv.core.domain.R.string.primary),
    Secondary(com.w2sv.core.domain.R.string.secondary);

    fun getColor(data: WidgetColoring.Style.Custom): Color =
        Color(
            when (this) {
                Background -> data.background
                Primary -> data.primary
                Secondary -> data.secondary
            }
        )
}

@Composable
private fun CustomColorConfiguration(
    data: WidgetColoring.Style.Custom,
    showCustomColorConfigurationDialog: (ColorPickerProperties) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            ColorPickerProperties(
                                customWidgetColor = widgetColorType,
                                appliedColor = color
                            )
                        )
                    },
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
    }
}

@Composable
private fun SectionCustomizationRow(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.weight(0.2f))
        Text(
            text = remember(label) { bulletPointText(label) },
            fontSize = 14.sp,
            modifier = Modifier.weight(0.4f),
        )
        val colorPickerButtonCD =
            stringResource(
                id = R.string.color_picker_button_cd,
                label
            )
        Button(
            modifier = modifier
                .size(40.dp)
                .semantics { contentDescription = colorPickerButtonCD },
            colors = ButtonDefaults.buttonColors(
                containerColor = color,
            ),
            onClick = onClick,
            shape = CircleShape,
            content = {},
        )
        Spacer(modifier = Modifier.weight(0.2f))
    }
}