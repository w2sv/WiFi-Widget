package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.content.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w2sv.data.model.Theme
import com.w2sv.data.model.widget.WidgetColor
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.ButtonColor
import com.w2sv.wifiwidget.ui.components.ThemeIndicatorProperties
import com.w2sv.wifiwidget.ui.components.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.components.UseDynamicColorsRow
import com.w2sv.wifiwidget.ui.components.dynamicColorsSupported
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.content.components.colors.ColorSelection
import com.w2sv.wifiwidget.ui.utils.EPSILON
import com.w2sv.wifiwidget.ui.utils.circularTrifoldStripeBrush
import com.w2sv.wifiwidget.ui.utils.toColor

@Composable
fun ThemeSelection(
    theme: Theme,
    customThemeSelected: Boolean,
    setTheme: (Theme) -> Unit,
    useDynamicColors: Boolean,
    setUseDynamicColors: (Boolean) -> Unit,
    customColorsMap: MutableMap<WidgetColor, Int>
) {
    Column {
        val customThemeIndicatorWeight by animateFloatAsState(
            targetValue = if (useDynamicColors) EPSILON else 1f,
            label = ""
        )

        ThemeSelectionRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ((1 - customThemeIndicatorWeight) * 32).dp),
            customThemeIndicatorProperties = ThemeIndicatorProperties(
                theme = Theme.Custom,
                labelRes = R.string.custom,
                buttonColoring = ButtonColor.Gradient(
                    circularTrifoldStripeBrush(
                        customColorsMap.getValue(WidgetColor.Background)
                            .toColor(),
                        customColorsMap.getValue(WidgetColor.Primary)
                            .toColor(),
                        customColorsMap.getValue(WidgetColor.Secondary)
                            .toColor()
                    )
                )
            ),
            selected = theme,
            onSelected = setTheme,
            themeWeights = mapOf(Theme.Custom to customThemeIndicatorWeight),
            themeIndicatorModifier = Modifier
                .padding(horizontal = 12.dp)
                .sizeIn(maxHeight = 92.dp)
        )

        AnimatedVisibility(visible = customThemeSelected) {
            ColorSelection(
                widgetColors = customColorsMap,
                modifier = Modifier
                    .padding(top = 18.dp)
            )
        }

        if (dynamicColorsSupported) {
            UseDynamicColorsRow(
                useDynamicColors = useDynamicColors,
                onToggleDynamicColors = {
                    setUseDynamicColors(it)
                    if (it && customThemeSelected) {
                        setTheme(Theme.SystemDefault)
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .padding(top = 22.dp)
            )
        }
    }
}