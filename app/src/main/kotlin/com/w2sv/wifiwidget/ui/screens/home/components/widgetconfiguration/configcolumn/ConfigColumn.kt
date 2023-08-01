package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutElastic
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.data.model.Theme
import com.w2sv.data.model.WidgetColor
import com.w2sv.data.model.WifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.ButtonColor
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.ThemeIndicatorProperties
import com.w2sv.wifiwidget.ui.components.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.screens.home.HomeScreenViewModel
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.WidgetViewModel
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.ColorSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.PropertySelection
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.RefreshingParametersSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.SliderWithLabel
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.utils.circularTrifoldStripeBrush
import com.w2sv.wifiwidget.ui.utils.toColor

@Preview
@Composable
private fun Prev() {
    AppTheme {
        ConfigColumn()
    }
}

@Composable
fun ConfigColumn(
    modifier: Modifier = Modifier,
    widgetConfigurationVM: WidgetViewModel = viewModel(),
    homeScreenVM: HomeScreenViewModel = viewModel()
) {
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(scrollState)
    ) {
        val defaultSectionHeaderModifier = Modifier.padding(vertical = 22.dp)

        SectionHeader(
            titleRes = R.string.theme,
            iconRes = R.drawable.ic_nightlight_24,
            modifier = Modifier.padding(bottom = 22.dp)
        )
        ThemeSelectionRow(
            modifier = Modifier.fillMaxWidth(),
            customThemeIndicatorProperties = ThemeIndicatorProperties(
                theme = Theme.Custom,
                label = R.string.custom,
                buttonColoring = ButtonColor.Gradient(
                    circularTrifoldStripeBrush(
                        widgetConfigurationVM.customColorsMap.getValue(WidgetColor.Background)
                            .toColor(),
                        widgetConfigurationVM.customColorsMap.getValue(WidgetColor.Primary)
                            .toColor(),
                        widgetConfigurationVM.customColorsMap.getValue(WidgetColor.Secondary)
                            .toColor()
                    )
                )
            ),
            selected = widgetConfigurationVM.theme.collectAsState().value,
            onSelected = {
                widgetConfigurationVM.theme.value = it
            }
        )

        AnimatedVisibility(
            visible = widgetConfigurationVM.customThemeSelected.collectAsState(false).value,
            enter = fadeIn(animationSpec = tween(1000)) +
                    expandVertically(
                        animationSpec = tween(
                            1000,
                            easing = EaseOutElastic
                        )
                    ),
            exit = fadeOut(animationSpec = tween(1000)) +
                    shrinkVertically(
                        animationSpec = tween(
                            1000,
                            easing = EaseOutElastic
                        )
                    )
        ) {
            ColorSelection(
                widgetColors = widgetConfigurationVM.customColorsMap,
                modifier = Modifier
                    .padding(top = 18.dp)
            )
        }

        SectionHeader(
            titleRes = R.string.opacity,
            iconRes = R.drawable.ic_opacity_24,
            modifier = defaultSectionHeaderModifier
        )
        SliderWithLabel(
            opacity = widgetConfigurationVM.opacity.collectAsState().value,
            onOpacityChanged = {
                widgetConfigurationVM.opacity.value = it
            },
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        SectionHeader(
            titleRes = R.string.properties,
            iconRes = R.drawable.ic_checklist_24,
            modifier = defaultSectionHeaderModifier
        )
        PropertySelection(
            propertyChecked = { property ->
                widgetConfigurationVM.setWifiProperties.getValue(property)
            },
            onCheckedChange = { property, value ->
                when (property == WifiProperty.SSID && value) {
                    true -> when (homeScreenVM.lapRationalShown) {
                        false -> homeScreenVM.lapRationalTrigger.value =
                            LocationAccessPermissionRequestTrigger.SSIDCheck

                        true -> homeScreenVM.lapRequestTrigger.value =
                            LocationAccessPermissionRequestTrigger.SSIDCheck
                    }

                    false -> widgetConfigurationVM.setWifiProperties[property] =
                        value
                }
            },
            onInfoButtonClick = { widgetConfigurationVM.infoDialogProperty.value = it }
        )

        SectionHeader(
            titleRes = R.string.refreshing,
            iconRes = com.w2sv.widget.R.drawable.ic_refresh_24,
            modifier = defaultSectionHeaderModifier
        )
        RefreshingParametersSelection(
            widgetRefreshingMap = widgetConfigurationVM.refreshingParametersMap,
            scrollToContentColumnBottom = {
                with(scrollState) {
                    animateScrollTo(maxValue)
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(
    @StringRes titleRes: Int,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(0.3f), contentAlignment = Alignment.Center) {
            Icon(
                painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        Box(modifier = Modifier.weight(0.7f), contentAlignment = Alignment.Center) {
            JostText(
                text = stringResource(id = titleRes),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.weight(0.3f))
    }
}