package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.domain.model.WidgetBottomRowElement
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.designsystem.IconHeaderProperties
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.AppearanceConfiguration
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.ColorPickerProperties
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.PropertyCheckRowColumn
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.InfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.UnconfirmedWidgetConfiguration
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.getInfoDialogData
import com.w2sv.wifiwidget.ui.utils.ShakeConfig
import com.w2sv.wifiwidget.ui.utils.ShakeController
import com.w2sv.wifiwidget.ui.utils.shake
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

private val verticalSectionHeaderPadding = 18.dp
private val subPropertyCheckRowColumnModifier: Modifier = Modifier.padding(horizontal = 16.dp)
private val checkRowColumnBottomPadding = 8.dp

@Immutable
private data class Section(
    val iconHeaderProperties: IconHeaderProperties,
    val headerModifier: Modifier = Modifier.padding(vertical = verticalSectionHeaderPadding),
    val content: @Composable () -> Unit
)

@Composable
fun WidgetConfigurationDialogContent(
    widgetConfiguration: UnconfirmedWidgetConfiguration,
    locationAccessState: LocationAccessState,
    showPropertyInfoDialog: (InfoDialogData) -> Unit,
    showCustomColorConfigurationDialog: (ColorPickerProperties) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .verticalScroll(rememberScrollState()),
    ) {
        val context: Context = LocalContext.current

        remember {
            persistentListOf(
                Section(
                    iconHeaderProperties = IconHeaderProperties(
                        iconRes = R.drawable.ic_palette_24,
                        stringRes = R.string.appearance
                    ),
                ) {
                    AppearanceConfiguration(
                        presetColoringData = widgetConfiguration.presetColoringData.collectAsStateWithLifecycle().value,
                        setPresetColoringData = {
                            widgetConfiguration.presetColoringData.value = it
                        },
                        customColoringData = widgetConfiguration.customColoringData.collectAsStateWithLifecycle().value,
                        coloring = widgetConfiguration.coloring.collectAsStateWithLifecycle().value,
                        setColoring = { widgetConfiguration.coloring.value = it },
                        opacity = widgetConfiguration.opacity.collectAsStateWithLifecycle().value,
                        setOpacity = {
                            widgetConfiguration.opacity.value = it
                        },
                        fontSize = widgetConfiguration.fontSize.collectAsStateWithLifecycle().value,
                        setFontSize = { widgetConfiguration.fontSize.value = it },
                        showCustomColorConfigurationDialog = showCustomColorConfigurationDialog,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                },
                Section(
                    IconHeaderProperties(
                        iconRes = R.drawable.ic_checklist_24,
                        stringRes = R.string.properties
                    )
                ) {
                    PropertyCheckRowColumn(
                        dataList = rememberWidgetWifiPropertyCheckRowData(
                            widgetConfiguration = widgetConfiguration,
                            locationAccessState = locationAccessState
                        ),
                        showInfoDialog = showPropertyInfoDialog,
                    )
                },
                Section(
                    iconHeaderProperties = IconHeaderProperties(
                        iconRes = R.drawable.ic_bottom_row_24,
                        stringRes = R.string.bottom_row,
                    )
                ) {
                    PropertyCheckRowColumn(
                        dataList = remember {
                            WidgetBottomRowElement.entries.map {
                                PropertyCheckRowData.WithoutSubProperties.fromIsCheckedMap(
                                    property = it,
                                    isCheckedMap = widgetConfiguration.bottomRowMap
                                )
                            }
                                .toPersistentList()
                        },
                        modifier = Modifier.padding(bottom = checkRowColumnBottomPadding)
                    )
                },
                Section(
                    iconHeaderProperties = IconHeaderProperties(
                        iconRes = com.w2sv.widget.R.drawable.ic_refresh_24,
                        stringRes = R.string.refreshing,
                    )
                ) {
                    PropertyCheckRowColumn(
                        dataList = remember {
                            persistentListOf(
                                PropertyCheckRowData.WithSubProperties.fromIsCheckedMap(
                                    property = WidgetRefreshingParameter.RefreshPeriodically,
                                    isCheckedMap = widgetConfiguration.refreshingParametersMap,
                                    infoDialogData = InfoDialogData(
                                        title = context.getString(WidgetRefreshingParameter.RefreshPeriodically.labelRes),
                                        description = context.getString(R.string.refresh_periodically_info)
                                    ),
                                    subPropertyCheckRowDataList = persistentListOf(
                                        PropertyCheckRowData.WithoutSubProperties.fromIsCheckedMap(
                                            property = WidgetRefreshingParameter.RefreshOnLowBattery,
                                            isCheckedMap = widgetConfiguration.refreshingParametersMap
                                        )
                                    ),
                                    subPropertyCheckRowColumnModifier = subPropertyCheckRowColumnModifier
                                )
                            )
                        },
                        showInfoDialog = showPropertyInfoDialog,
                        modifier = Modifier.padding(bottom = checkRowColumnBottomPadding)
                    )
                }
            )
        }
            .forEach { section ->
                Column(
                    modifier = Modifier
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            MaterialTheme.shapes.medium
                        )
                ) {
                    IconHeader(
                        properties = section.iconHeaderProperties,
                        modifier = section.headerModifier.padding(horizontal = 32.dp),
                    )
                    section.content()
                }
            }
    }
}

@Composable
private fun rememberWidgetWifiPropertyCheckRowData(
    widgetConfiguration: UnconfirmedWidgetConfiguration,
    locationAccessState: LocationAccessState,
): ImmutableList<PropertyCheckRowData<WidgetWifiProperty>> {
    val context = LocalContext.current
    return remember {
        WidgetWifiProperty.entries
            .map { property ->
                val shakeController = ShakeController(shakeConfig)

                when (property) {
                    is WidgetWifiProperty.NonIP -> {
                        PropertyCheckRowData.WithoutSubProperties.fromIsCheckedMap(
                            property = property,
                            isCheckedMap = widgetConfiguration.wifiProperties,
                            allowCheckChange = when (property) {
                                is WidgetWifiProperty.NonIP.LocationAccessRequiring -> { isCheckedNew ->
                                    (if (isCheckedNew) {
                                        locationAccessState.run {
                                            isGranted.also {
                                                if (!it) {
                                                    launchRequest(
                                                        LocationAccessPermissionRequestTrigger.PropertyCheckChange(
                                                            property,
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        widgetConfiguration.moreThanOnePropertyChecked()
                                    })
                                        .also {
                                            if (!it) {
                                                shakeController.shake()
                                            }
                                        }
                                }

                                else -> { isCheckedNew ->
                                    (isCheckedNew || widgetConfiguration.moreThanOnePropertyChecked()).also {
                                        if (!it) {
                                            shakeController
                                                .shake()
                                        }
                                    }
                                }
                            },
                            infoDialogData = property.getInfoDialogData(context),
                            modifier = Modifier.shake(shakeController)
                        )
                    }

                    is WidgetWifiProperty.IP -> {
                        PropertyCheckRowData.WithSubProperties.fromIsCheckedMap(
                            property = property,
                            isCheckedMap = widgetConfiguration.wifiProperties,
                            allowCheckChange = {
                                widgetConfiguration.moreThanOnePropertyChecked()
                                    .also {
                                        if (!it) {
                                            shakeController.shake()
                                        }
                                    }
                            },
                            subPropertyCheckRowDataList = property.subProperties
                                .map { subProperty ->
                                    val subPropertyShakeController =
                                        if (subProperty.isAddressTypeEnablementProperty)
                                            ShakeController(shakeConfig)
                                        else
                                            null

                                    PropertyCheckRowData.WithoutSubProperties.fromIsCheckedMap(
                                        property = subProperty,
                                        isCheckedMap = widgetConfiguration.ipSubProperties,
                                        allowCheckChange = { newValue ->
                                            subProperty.allowCheckChange(
                                                newValue,
                                                widgetConfiguration.ipSubProperties
                                            )
                                                .also {
                                                    if (!it) {
                                                        subPropertyShakeController?.shake()
                                                    }
                                                }
                                        },
                                        modifier = subPropertyShakeController?.let {
                                            Modifier.shake(
                                                it
                                            )
                                        }
                                            ?: Modifier
                                    )
                                }
                                .toPersistentList(),
                            subPropertyCheckRowColumnModifier = subPropertyCheckRowColumnModifier,
                            infoDialogData = property.getInfoDialogData(context),
                            modifier = Modifier.shake(shakeController)
                        )
                    }
                }
            }
            .toPersistentList()
    }
}

private fun UnconfirmedWidgetConfiguration.moreThanOnePropertyChecked(): Boolean =
    wifiProperties.values.count { it } > 1

private val shakeConfig = ShakeConfig(
    iterations = 2,
    translateX = 12.5f,
    stiffness = 20_000f
)

private fun WidgetWifiProperty.IP.SubProperty.allowCheckChange(
    newValue: Boolean,
    subPropertyEnablementMap: Map<WidgetWifiProperty.IP.SubProperty, Boolean>
): Boolean =
    when (val capturedKind = kind) {
        is WidgetWifiProperty.IP.V4AndV6.AddressTypeEnablement -> {
            newValue || subPropertyEnablementMap.getValue(
                WidgetWifiProperty.IP.SubProperty(
                    property = property,
                    kind = capturedKind.opposingAddressTypeEnablement
                )
            )
        }

        else -> true
    }
