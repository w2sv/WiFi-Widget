package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.configcolumn

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.androidutils.extensions.requireCastActivity
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.common.CustomizableWidgetSection
import com.w2sv.common.Theme
import com.w2sv.common.WifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.HomeActivity
import com.w2sv.wifiwidget.ui.screens.home.LocationAccessPermissionDialogTrigger
import com.w2sv.wifiwidget.ui.shared.ButtonColoring
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.ThemeIndicatorProperties
import com.w2sv.wifiwidget.ui.shared.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme

@Preview
@Composable
private fun Prev() {
    WifiWidgetTheme {
        ConfigColumn()
    }
}

@Composable
fun ConfigColumn(
    modifier: Modifier = Modifier,
    viewModel: HomeActivity.ViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val scrollState = rememberScrollState()

    val showCustomColorSection by viewModel.showCustomThemeSection.collectAsState(false)
    val theme by viewModel.widgetThemeState.collectAsState()
    val opacity by viewModel.widgetOpacityState.collectAsState()

    val context = LocalContext.current
    val lapRequestLauncher = context.requireCastActivity<HomeActivity>().lapRequestLauncher

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(vertical = 16.dp)
            .verticalScroll(scrollState)
    ) {
        val checkablePropertiesColumnModifier = Modifier.padding(horizontal = 26.dp)
        val defaultSectionHeaderModifier = Modifier.padding(vertical = 22.dp)

        SectionHeader(
            titleRes = R.string.theme,
            iconRes = R.drawable.ic_nightlight_24,
            modifier = Modifier.padding(top = 12.dp, bottom = 22.dp)
        )
        ThemeSelectionRow(modifier = Modifier.fillMaxWidth(),
            customThemeIndicatorProperties = ThemeIndicatorProperties(
                theme = Theme.Custom,
                label = R.string.custom,
                buttonColoring = ButtonColoring.Gradient(
                    Brush.linearGradient(
                        0.4f to Color(viewModel.customWidgetColorsState[CustomizableWidgetSection.Background.name]!!),
                        0.4f to Color(viewModel.customWidgetColorsState[CustomizableWidgetSection.Labels.name]!!),
                        0.6f to Color(viewModel.customWidgetColorsState[CustomizableWidgetSection.Labels.name]!!),
                        0.6f to Color(viewModel.customWidgetColorsState[CustomizableWidgetSection.Values.name]!!)
                    )
                )
            ),
            selected = {
                theme
            },
            onSelected = {
                viewModel.widgetThemeState.value = it
            }
        )

        AnimatedVisibility(visible = showCustomColorSection) {
            ColorSelectionRow(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 18.dp)
            )
        }

        SectionHeader(
            R.string.opacity, R.drawable.ic_opacity_24, defaultSectionHeaderModifier
        )
        OpacitySliderWithValue(opacity = { opacity }, onOpacityChanged = {
            viewModel.widgetOpacityState.value = it
        })

        SectionHeader(
            titleRes = R.string.properties,
            iconRes = R.drawable.ic_checklist_24,
            modifier = defaultSectionHeaderModifier
        )
        PropertySelectionSection(modifier = checkablePropertiesColumnModifier,
            propertyChecked = { property ->
                viewModel.widgetPropertyStateMap.getValue(property.name)
            },
            onCheckedChange = { property, value ->
                when {
                    property == WifiProperty.SSID && value -> {
                        when (viewModel.lapDialogAnswered) {
                            false -> viewModel.lapDialogTrigger.value =
                                LocationAccessPermissionDialogTrigger.SSIDCheck

                            true -> lapRequestLauncher.requestPermissionAndSetSSIDFlagCorrespondingly(
                                viewModel
                            )
                        }
                    }

                    else -> viewModel.confirmAndSyncPropertyChange(property, value) {
                        context.showToast(R.string.uncheck_all_properties_toast)
                    }
                }
            },
            onInfoButtonClick = { viewModel.propertyInfoDialogIndex.value = it })

        SectionHeader(
            titleRes = R.string.refreshing,
            iconRes = com.w2sv.widget.R.drawable.ic_refresh_24,
            modifier = defaultSectionHeaderModifier
        )
        RefreshingSection(checkablePropertiesColumnModifier) {
            with(scrollState) {
                animateScrollTo(maxValue)
            }
        }
    }
}

@Composable
private fun SectionHeader(
    @StringRes titleRes: Int,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(0.6f), contentAlignment = Alignment.Center) {
            Icon(
                painterResource(id = iconRes),
                contentDescription = "@null",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.Center) {
            JostText(
                text = stringResource(id = titleRes),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.weight(0.6f))
    }
}