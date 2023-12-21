package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog

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
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.IconHeader
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.OpacitySliderWithLabel
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.PropertyCheckRows
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.PropertyInfoDialog
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.ThemeSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyInfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.UnconfirmedWidgetConfiguration
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.infoDialogData
import com.w2sv.wifiwidget.ui.utils.toColor
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Composable
fun WidgetConfigurationDialogContent(
    widgetConfiguration: UnconfirmedWidgetConfiguration,
    locationAccessState: LocationAccessState,
    modifier: Modifier = Modifier,
) {
    var propertyInfoDialogData by remember {  // TODO: make rememberSavable
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
            .verticalScroll(rememberScrollState()),
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

        fun moreThanOneWifiPropertyChecked(): Boolean =
            widgetConfiguration.wifiProperties.values.count { it } > 1

        PropertyCheckRows(
            remember {
                WidgetWifiProperty.entries
                    .map { property ->
                        when (property) {
                            is WidgetWifiProperty.NonIP.LocationAccessRequiring -> PropertyCheckRowData.fromMutableMap(
                                property = property,
                                isCheckedMap = widgetConfiguration.wifiProperties,
                                allowCheckChange = { newValue ->
                                    if (newValue) {
                                        (locationAccessState.isGranted).also {
                                            if (!it) {
                                                locationAccessState.launchRequest(
                                                    LocationAccessPermissionRequestTrigger.PropertyCheckChange(
                                                        property,
                                                    )
                                                )
                                            }
                                        }
                                    } else {
                                        moreThanOneWifiPropertyChecked()
                                    }
                                },
                                infoDialogData = property.infoDialogData
                            )

                            is WidgetWifiProperty.IP -> {
                                PropertyCheckRowData.fromMutableMap(
                                    property = property,
                                    isCheckedMap = widgetConfiguration.wifiProperties,
                                    allowCheckChange = { isCheckedNew ->
                                        isCheckedNew || moreThanOneWifiPropertyChecked()
                                    },
                                    subPropertyCheckRowData = property.subProperties
                                        .map { subProperty ->
                                            PropertyCheckRowData.fromMutableMap(
                                                property = subProperty,
                                                isCheckedMap = widgetConfiguration.subWifiProperties,
                                                allowCheckChange = { newValue ->
                                                    (property as? WidgetWifiProperty.IP.V4AndV6)?.let { v4AndV6Property ->
                                                        newValue || !subProperty.isAddressTypeEnablementProperty || v4AndV6Property.addressTypeEnablementSubProperties.all {
                                                            widgetConfiguration.subWifiProperties.getValue(
                                                                it
                                                            )
                                                        }
                                                    } ?: true
                                                }
                                            )
                                        }
                                        .toPersistentList(),
                                    infoDialogData = property.infoDialogData
                                )
                            }

                            is WidgetWifiProperty.NonIP.Other -> PropertyCheckRowData.fromMutableMap(
                                property = property,
                                isCheckedMap = widgetConfiguration.wifiProperties,
                                allowCheckChange = { isCheckedNew ->
                                    isCheckedNew || moreThanOneWifiPropertyChecked()
                                },
                                infoDialogData = property.infoDialogData,
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
        PropertyCheckRows(
            dataList = remember {
                WidgetButton.entries.map {
                    PropertyCheckRowData.fromMutableMap(
                        property = it,
                        isCheckedMap = widgetConfiguration.buttonMap
                    )
                }
                    .toPersistentList()
            }
        )

        SectionHeader(
            iconRes = com.w2sv.widget.R.drawable.ic_refresh_24,
            headerRes = R.string.refreshing,
        )
        PropertyCheckRows(
            dataList = remember {
                persistentListOf(
                    PropertyCheckRowData.fromMutableMap(
                        property = WidgetRefreshingParameter.RefreshPeriodically,
                        isCheckedMap = widgetConfiguration.refreshingParametersMap,
                        infoDialogData = PropertyInfoDialogData(
                            labelRes = WidgetRefreshingParameter.RefreshPeriodically.labelRes,
                            descriptionRes = R.string.refresh_periodically_info
                        ),
                        subPropertyCheckRowData = persistentListOf(
                            PropertyCheckRowData.fromMutableMap(
                                property = WidgetRefreshingParameter.RefreshOnLowBattery,
                                isCheckedMap = widgetConfiguration.refreshingParametersMap
                            )
                        )
                    ),
                    PropertyCheckRowData.fromMutableMap(
                        property = WidgetRefreshingParameter.DisplayLastRefreshDateTime,
                        isCheckedMap = widgetConfiguration.refreshingParametersMap
                    )
                )
            },
            showInfoDialog = ::showInfoDialog
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
