package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.content

import android.annotation.SuppressLint
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequiringAction
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionUIState
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.content.components.ButtonSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.content.components.OpacitySliderWithLabel
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.content.components.RefreshingParametersSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.content.components.ThemeSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.content.components.WifiPropertySelection
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.model.UnconfirmedWidgetConfiguration

@Composable
internal fun WidgetConfigurationDialogContent(
    widgetConfiguration: UnconfirmedWidgetConfiguration,
    showInfoDialog: (InfoDialogData) -> Unit,
    lapUIState: LocationAccessPermissionUIState,
    modifier: Modifier = Modifier
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
            theme = widgetConfiguration.theme.collectAsState().value,
            customThemeSelected = widgetConfiguration.customThemeSelected.collectAsState(initial = false).value,
            setTheme = { widgetConfiguration.theme.value = it },
            useDynamicColors = widgetConfiguration.useDynamicColors.collectAsState().value,
            setUseDynamicColors = { widgetConfiguration.useDynamicColors.value = it },
            customColorsMap = widgetConfiguration.customColorsMap
        )

        SectionHeader(
            titleRes = R.string.opacity,
            iconRes = R.drawable.ic_opacity_24,
        )
        OpacitySliderWithLabel(
            opacity = widgetConfiguration.opacity.collectAsState().value,
            onOpacityChanged = {
                widgetConfiguration.opacity.value = it
            },
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        SectionHeader(
            titleRes = R.string.properties,
            iconRes = R.drawable.ic_checklist_24,
        )
        WifiPropertySelection(
            wifiPropertiesMap = widgetConfiguration.wifiProperties,
            ipSubPropertiesMap = widgetConfiguration.subWifiProperties,
            allowLAPDependentPropertyCheckChange = { property, newValue ->
                when (newValue) {
                    true -> {
                        when (lapUIState.rationalShown) {
                            false -> {
                                lapUIState.rationalTriggeringAction.value =
                                    LocationAccessPermissionRequiringAction.PropertyCheckChange(
                                        property
                                    )
                            }

                            true -> {
                                lapUIState.requestLaunchingAction.value =
                                    LocationAccessPermissionRequiringAction.PropertyCheckChange(
                                        property
                                    )
                            }
                        }
                        false
                    }

                    false -> true
                }
            },
            showInfoDialog = showInfoDialog
        )

        SectionHeader(
            titleRes = R.string.buttons,
            iconRes = R.drawable.ic_gamepad_24,
        )
        ButtonSelection(widgetConfiguration.buttonMap)

        SectionHeader(
            titleRes = R.string.refreshing,
            iconRes = com.w2sv.widget.R.drawable.ic_refresh_24,
        )
        RefreshingParametersSelection(
            widgetRefreshingMap = widgetConfiguration.refreshingParametersMap,
            scrollToContentColumnBottom = {
                with(scrollState) {
                    animateScrollTo(maxValue)
                }
            },
            showInfoDialog = showInfoDialog
        )
    }
}

@Composable
private fun SectionHeader(
    @StringRes titleRes: Int,
    @DrawableRes iconRes: Int,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier.padding(vertical = 22.dp)
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(0.3f), contentAlignment = Alignment.Center) {
            Icon(
                painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
        Box(modifier = Modifier.weight(0.7f), contentAlignment = Alignment.Center) {
            JostText(
                text = stringResource(id = titleRes),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        Spacer(modifier = Modifier.weight(0.3f))
    }
}