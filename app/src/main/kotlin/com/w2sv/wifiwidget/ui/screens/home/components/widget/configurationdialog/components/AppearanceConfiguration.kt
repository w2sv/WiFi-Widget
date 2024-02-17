package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.utils.bulletPointText
import com.w2sv.common.utils.dynamicColorsSupported
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
    presetColoringData: WidgetColoring.Data.Preset,
    setPresetColoringData: (WidgetColoring.Data.Preset) -> Unit,
    customColoringData: WidgetColoring.Data.Custom,
    setCustomColoringData: (WidgetColoring.Data.Custom) -> Unit,
    coloring: WidgetColoring,
    setColoring: (WidgetColoring) -> Unit,
    opacity: Float,
    setOpacity: (Float) -> Unit,
    fontSize: FontSize,
    setFontSize: (FontSize) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .padding(bottom = verticalPadding)
                .align(Alignment.CenterHorizontally)
        ) {
            WidgetColoring.entries.forEach {
                SegmentedButton(
                    selected = it == coloring,
                    onClick = { setColoring(it) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = it.ordinal,
                        count = WidgetColoring.entries.size
                    )
                ) {
                    Text(text = stringResource(id = it.labelRes))
                }
            }
        }

        AnimatedContent(targetState = coloring, label = "") {
            when (it) {
                WidgetColoring.Preset -> {
                    PresetColoringConfiguration(
                        data = presetColoringData,
                        setData = setPresetColoringData
                    )
                }

                WidgetColoring.Custom -> {
                    CustomColorConfiguration(
                        data = customColoringData,
                        setData = setCustomColoringData
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
    data: WidgetColoring.Data.Preset,
    setData: (WidgetColoring.Data.Preset) -> Unit,
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

private data class CustomColor(
    @StringRes val labelRes: Int,
    val color: Color,
    val setColor: (Color) -> Unit
)

@Composable
private fun CustomColorConfiguration(
    data: WidgetColoring.Data.Custom,
    setData: (WidgetColoring.Data.Custom) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialogFor by remember {
        mutableStateOf<CustomColor?>(null)
    }
        .apply {
            value?.let { customColor ->
                ColorPickerDialog(
                    label = stringResource(id = customColor.labelRes),
                    appliedColor = customColor.color,
                    applyColor = { customColor.setColor(it) },
                    onDismissRequest = {
                        value = null
                    },
                )
            }
        }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        remember(data) {
            listOf(
                CustomColor(
                    labelRes = com.w2sv.domain.R.string.background,
                    color = Color(data.background),
                    setColor = { setData(data.copy(background = it.toArgb())) }
                ),
                CustomColor(
                    labelRes = com.w2sv.domain.R.string.primary,
                    color = Color(data.primary),
                    setColor = { setData(data.copy(primary = it.toArgb())) }
                ),
                CustomColor(
                    labelRes = com.w2sv.domain.R.string.secondary,
                    color = Color(data.secondary),
                    setColor = { setData(data.copy(secondary = it.toArgb())) }
                )
            )
        }
            .forEach {
                SectionCustomizationRow(
                    label = stringResource(id = it.labelRes),
                    color = it.color,
                    onClick = { showDialogFor = it },
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
            text = bulletPointText(label),
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