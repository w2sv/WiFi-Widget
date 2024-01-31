package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.w2sv.common.utils.dynamicColorsSupported
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetColorSection
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.ButtonColor
import com.w2sv.wifiwidget.ui.components.ThemeIndicatorProperties
import com.w2sv.wifiwidget.ui.components.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.components.UseDynamicColorsRow
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.colors.ColorSelection
import com.w2sv.wifiwidget.ui.utils.EPSILON
import com.w2sv.wifiwidget.ui.utils.circularTrifoldStripeBrush
import kotlinx.collections.immutable.persistentMapOf
import kotlin.math.roundToInt

private val verticalPadding = 12.dp

@Composable
fun AppearanceSelection(
    theme: Theme,
    customThemeSelected: Boolean,
    setTheme: (Theme) -> Unit,
    useDynamicColors: Boolean,
    setUseDynamicColors: (Boolean) -> Unit,
    getCustomColor: (WidgetColorSection) -> Color,
    setCustomColor: (WidgetColorSection, Color) -> Unit,
    opacity: Float,
    onOpacityChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val customThemeIndicatorWeight by animateFloatAsState(
            targetValue = if (useDynamicColors) EPSILON else 1f,
            label = "",
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
                        getCustomColor(WidgetColorSection.Background),
                        getCustomColor(WidgetColorSection.Primary),
                        getCustomColor(WidgetColorSection.Secondary),
                    ),
                ),
            ),
            selected = theme,
            onSelected = setTheme,
            themeWeights = persistentMapOf(Theme.Custom to customThemeIndicatorWeight),
            themeIndicatorModifier = Modifier
                .padding(horizontal = 12.dp)
                .sizeIn(maxHeight = 92.dp),
        )

        AnimatedVisibility(visible = customThemeSelected) {
            ColorSelection(
                getCustomColor = getCustomColor,
                setCustomColor = setCustomColor,
                modifier = Modifier
                    .padding(top = verticalPadding),
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
                    .padding(top = verticalPadding),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = verticalPadding)
        ) {
            Text(text = stringResource(R.string.background_opacity))
            Spacer(modifier = Modifier.width(12.dp))
            OpacitySliderWithLabel(
                opacity = opacity,
                onOpacityChanged = onOpacityChanged,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OpacitySliderWithLabel(
    opacity: Float,
    onOpacityChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "${(opacity * 100).roundToInt()}%",
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        val context = LocalContext.current
        Slider(
            value = opacity,
            onValueChange = onOpacityChanged,
            modifier = Modifier
                .semantics {
                    contentDescription = context.getString(
                        R.string.opacity_slider_cd,
                    )
                },
            steps = 9,
        )
    }
}