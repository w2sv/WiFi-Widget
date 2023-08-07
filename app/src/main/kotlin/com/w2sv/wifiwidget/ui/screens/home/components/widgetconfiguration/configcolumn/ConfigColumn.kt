package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LAPRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.ButtonSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.OpacitySliderWithLabel
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.RefreshingParametersSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.ThemeSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.WifiPropertySelection
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.viewmodels.HomeScreenViewModel
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel

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
    widgetVM: WidgetViewModel = viewModel(),
    homeScreenVM: HomeScreenViewModel = viewModel()
) {
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(scrollState)
    ) {
        SectionHeader(
            titleRes = R.string.theme,
            iconRes = R.drawable.ic_nightlight_24,
            modifier = Modifier.padding(bottom = 22.dp)
        )

        ThemeSelection(
            theme = widgetVM.theme.collectAsState().value,
            customThemeSelected = widgetVM.customThemeSelected.collectAsState(initial = false).value,
            setTheme = { widgetVM.theme.value = it },
            useDynamicColors = widgetVM.useDynamicColors.collectAsState().value,
            setUseDynamicColors = { widgetVM.useDynamicColors.value = it },
            customColorsMap = widgetVM.customColorsMap
        )

        SectionHeader(
            titleRes = R.string.opacity,
            iconRes = R.drawable.ic_opacity_24,
        )
        OpacitySliderWithLabel(
            opacity = widgetVM.opacity.collectAsState().value,
            onOpacityChanged = {
                widgetVM.opacity.value = it
            },
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        SectionHeader(
            titleRes = R.string.properties,
            iconRes = R.drawable.ic_checklist_24,
        )
        WifiPropertySelection(
            wifiPropertiesMap = widgetVM.wifiProperties,
            ipSubPropertiesMap = widgetVM.subWifiProperties,
            allowLAPDependentPropertyCheckChange = { property, newValue ->
                when (newValue) {
                    true -> {
                        when (homeScreenVM.lapRationalShown) {
                            false -> {
                                homeScreenVM.lapRationalTrigger.value =
                                    LAPRequestTrigger.PropertyCheckChange(property)
                            }

                            true -> {
                                homeScreenVM.lapRequestTrigger.value =
                                    LAPRequestTrigger.PropertyCheckChange(property)
                            }
                        }
                        false
                    }

                    false -> true
                }
            },
            onInfoButtonClick = { widgetVM.infoDialogProperty.value = it }
        )

        SectionHeader(
            titleRes = R.string.buttons,
            iconRes = R.drawable.ic_gamepad_24,
        )
        ButtonSelection(widgetVM.buttonMap)

        SectionHeader(
            titleRes = R.string.refreshing,
            iconRes = com.w2sv.widget.R.drawable.ic_refresh_24,
        )
        RefreshingParametersSelection(
            widgetRefreshingMap = widgetVM.refreshingParametersMap,
            scrollToContentColumnBottom = {
                with(scrollState) {
                    animateScrollTo(maxValue)
                }
            },
            onRefreshPeriodicallyInfoIconClick = {
                widgetVM.refreshPeriodicallyInfoDialog.value = true
            }
        )
    }
}

@Composable
private fun SectionHeader(
    @StringRes titleRes: Int,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier.padding(vertical = 22.dp)
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
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