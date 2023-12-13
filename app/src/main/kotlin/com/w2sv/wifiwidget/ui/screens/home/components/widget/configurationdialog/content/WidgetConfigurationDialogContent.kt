package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.domain.model.WidgetButton
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.IconHeader
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionState
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components.ButtonSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components.OpacitySliderWithLabel
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components.RefreshingParametersSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components.ThemeSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components.WifiPropertySelection
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.IPPropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyInfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.UnconfirmedWidgetConfiguration
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.WifiPropertyCheckRowData
import com.w2sv.wifiwidget.ui.utils.toColor
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList

@Composable
fun WidgetConfigurationDialogContent(
    widgetConfiguration: UnconfirmedWidgetConfiguration,
    lapUIState: LocationAccessPermissionState,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    var propertyInfoDialogData by remember {
        mutableStateOf<PropertyInfoDialogData?>(null)
    }
        .apply {
            value?.let {
                PropertyInfoDialog(data = it, onDismissRequest = { value = null })
            }
        }

    fun showInfoDialog(data: PropertyInfoDialogData) {
        propertyInfoDialogData = data
    }

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
            customThemeSelected = widgetConfiguration.customThemeSelected.collectAsStateWithLifecycle().value,
            setTheme = { widgetConfiguration.theme.value = it },
            useDynamicColors = widgetConfiguration.useDynamicColors.collectAsStateWithLifecycle().value,
            setUseDynamicColors = { widgetConfiguration.useDynamicColors.value = it },
            getCustomColor = { widgetConfiguration.customColorsMap.getValue(it).toColor() },
            setCustomColor = { colorSection, color ->
                widgetConfiguration.customColorsMap[colorSection] = color.toArgb()
            }
        )

        SectionHeader(
            iconRes = R.drawable.ic_opacity_24,
            headerRes = R.string.opacity,
        )
        val opacity by widgetConfiguration.opacity.collectAsStateWithLifecycle()
        OpacitySliderWithLabel(
            getOpacity = { opacity },
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
            remember {
                WidgetWifiProperty.entries
                    .map { property ->
                        when (property) {
                            is WidgetWifiProperty.NonIP.LocationAccessRequiring -> WifiPropertyCheckRowData(
                                property = property,
                                isCheckedMap = widgetConfiguration.wifiProperties,
                                allowCheckChange = { newValue ->
                                    when (newValue) {
                                        true -> {
                                            lapUIState.setRequestTrigger(
                                                LocationAccessPermissionRequestTrigger.PropertyCheckChange(
                                                    property,
                                                )
                                            )
                                            false
                                        }

                                        false -> true
                                    }
                                }
                            )

                            is WidgetWifiProperty.IP -> {
                                IPPropertyCheckRowData(
                                    property = property,
                                    isCheckedMap = widgetConfiguration.wifiProperties,
                                    subPropertyIsCheckedMap = widgetConfiguration.subWifiProperties,
                                )
                            }

                            is WidgetWifiProperty.NonIP.Other -> WifiPropertyCheckRowData(
                                property = property,
                                isCheckedMap = widgetConfiguration.wifiProperties,
                            )
                        }
                    }
                    .toPersistentList()
            },
            showInfoDialog = ::showInfoDialog,
        )

        SectionHeader(
            iconRes = R.drawable.ic_gamepad_24,
            headerRes = R.string.buttons,
        )
        ButtonSelection(
            remember {
                WidgetButton.entries.map {
                    PropertyCheckRowData(
                        type = it,
                        labelRes = it.labelRes,
                        isCheckedMap = widgetConfiguration.buttonMap
                    )
                }
                    .toImmutableList()
            }
        )

        SectionHeader(
            iconRes = com.w2sv.widget.R.drawable.ic_refresh_24,
            headerRes = R.string.refreshing,
        )
        RefreshingParametersSelection(
            parameterIsChecked = { widgetConfiguration.refreshingParametersMap.getValue(it) },
            onParameterCheckedChanged = { parameter, value ->
                widgetConfiguration.refreshingParametersMap[parameter] = value
            },
            scrollToContentColumnBottom = {
                with(scrollState) {
                    animateScrollTo(maxValue)
                }
            },
            showInfoDialog = ::showInfoDialog,
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
