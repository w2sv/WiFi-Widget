package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.IconHeader
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequiringAction
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionState
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components.ButtonSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components.OpacitySliderWithLabel
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components.RefreshingParametersSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components.ThemeSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components.WifiPropertySelection
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyInfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.UnconfirmedWidgetConfiguration

@Composable
internal fun WidgetConfigurationDialogContent(
    widgetConfiguration: UnconfirmedWidgetConfiguration,
    showInfoDialog: (PropertyInfoDialogData) -> Unit,
    lapUIState: LocationAccessPermissionState,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(scrollState),
    ) {
        SectionHeader(
            iconRes = R.drawable.ic_nightlight_24,
            headerRes = R.string.theme,
            modifier = Modifier.padding(bottom = 22.dp),
        )

        ThemeSelection(
            theme = widgetConfiguration.theme.collectAsStateWithLifecycle().value,
            customThemeSelected = widgetConfiguration.customThemeSelected.collectAsStateWithLifecycle(
                false
            ).value,
            setTheme = { widgetConfiguration.theme.value = it },
            useDynamicColors = widgetConfiguration.useDynamicColors.collectAsStateWithLifecycle().value,
            setUseDynamicColors = { widgetConfiguration.useDynamicColors.value = it },
            customColorsMap = widgetConfiguration.customColorsMap,
        )

        SectionHeader(
            iconRes = R.drawable.ic_opacity_24,
            headerRes = R.string.opacity,
        )
        OpacitySliderWithLabel(
            opacity = widgetConfiguration.opacity.collectAsStateWithLifecycle().value,
            onOpacityChanged = {
                widgetConfiguration.opacity.value = it
            },
            modifier = Modifier.padding(horizontal = 6.dp),
        )

        SectionHeader(
            iconRes = R.drawable.ic_checklist_24,
            headerRes = R.string.properties,
        )
        WifiPropertySelection(
            wifiPropertiesMap = widgetConfiguration.wifiProperties,
            ipSubPropertiesMap = widgetConfiguration.subWifiProperties,
            allowLAPDependentPropertyCheckChange = { property, newValue ->
                when (newValue) {
                    true -> {
                        when (lapUIState.rationalShown) {
                            false -> {
                                lapUIState.setRationalTriggeringAction(
                                    LocationAccessPermissionRequiringAction.PropertyCheckChange(
                                        property,
                                    )
                                )
                            }

                            true -> {
                                lapUIState.setRequestLaunchingAction(
                                    LocationAccessPermissionRequiringAction.PropertyCheckChange(
                                        property,
                                    )
                                )
                            }
                        }
                        false
                    }

                    false -> true
                }
            },
            showInfoDialog = showInfoDialog,
        )

        SectionHeader(
            iconRes = R.drawable.ic_gamepad_24,
            headerRes = R.string.buttons,
        )
        ButtonSelection(widgetConfiguration.buttonMap)

        SectionHeader(
            iconRes = com.w2sv.widget.R.drawable.ic_refresh_24,
            headerRes = R.string.refreshing,
        )
        RefreshingParametersSelection(
            widgetRefreshingMap = widgetConfiguration.refreshingParametersMap,
            scrollToContentColumnBottom = {
                with(scrollState) {
                    animateScrollTo(maxValue)
                }
            },
            showInfoDialog = showInfoDialog,
        )
    }
}

@Composable
private fun SectionHeader(
    @DrawableRes iconRes: Int,
    @StringRes headerRes: Int,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier.padding(vertical = 22.dp),
) {
    IconHeader(
        iconRes = iconRes,
        headerRes = headerRes,
        modifier = modifier.padding(horizontal = 16.dp),
    )
}
