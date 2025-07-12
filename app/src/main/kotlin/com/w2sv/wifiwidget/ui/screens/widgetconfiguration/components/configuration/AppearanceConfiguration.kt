package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.PropertyValueAlignment
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.kotlinutils.enumEntryByOrdinal
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.designsystem.ArrowRightLabelContentRow
import com.w2sv.wifiwidget.ui.designsystem.SliderWithLabel
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.ColorPickerDialogData
import kotlin.math.roundToInt

object AppearanceConfigurationDefaults {
    val verticalPadding = 12.dp
}

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
            modifier = Modifier.padding(bottom = AppearanceConfigurationDefaults.verticalPadding)
        )
        BackgroundOpacitySliderRow(
            opacity = opacity,
            setOpacity = setOpacity,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppearanceConfigurationDefaults.verticalPadding)
        )
        FontSizeSliderRow(
            fontSize = fontSize,
            setFontSize = setFontSize,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppearanceConfigurationDefaults.verticalPadding)
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
                .padding(vertical = AppearanceConfigurationDefaults.verticalPadding)
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
                makeLabel = { "${(it * 100).roundToInt()}%" },
                onValueChanged = setOpacity,
                contentDescription = stringResource(id = R.string.opacity_slider_cd)
            )
        },
        modifier = modifier
    )
}
