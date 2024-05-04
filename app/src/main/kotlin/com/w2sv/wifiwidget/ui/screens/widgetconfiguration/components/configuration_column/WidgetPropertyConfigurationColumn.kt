package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration_column

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.common.utils.minutes
import com.w2sv.composed.isPortraitModeActive
import com.w2sv.domain.model.WidgetBottomRowElement
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.HomeScreenCardBackground
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.designsystem.IconHeaderProperties
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.designsystem.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.SubPropertyKeyboardArrowRightIcon
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.ColorPickerProperties
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.InfoDialogData
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.PropertyConfigurationView
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.ReversibleWidgetConfiguration
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.toInfoDialogData
import com.w2sv.wifiwidget.ui.utils.ShakeConfig
import com.w2sv.wifiwidget.ui.utils.ShakeController
import com.w2sv.wifiwidget.ui.utils.shake
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration

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
fun WidgetPropertyConfigurationColumn(
    widgetConfiguration: ReversibleWidgetConfiguration,
    locationAccessState: LocationAccessState,
    showPropertyInfoDialog: (InfoDialogData) -> Unit,
    showCustomColorConfigurationDialog: (ColorPickerProperties) -> Unit,
    showRefreshIntervalConfigurationDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = if (isPortraitModeActive) 26.dp else 126.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        SectionColumn(
            sections = rememberSections(
                widgetConfiguration = widgetConfiguration,
                locationAccessState = locationAccessState,
                showPropertyInfoDialog = showPropertyInfoDialog,
                showCustomColorConfigurationDialog = showCustomColorConfigurationDialog,
                showRefreshIntervalConfigurationDialog = showRefreshIntervalConfigurationDialog
            )
        )
        Spacer(modifier = Modifier.height(if (isPortraitModeActive) 142.dp else 92.dp))
    }
}

@Composable
private fun SectionColumn(sections: ImmutableList<Section>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        sections
            .forEach { section ->
                SectionCard(section = section)
            }
    }
}

@Composable
private fun SectionCard(section: Section, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(
                color = HomeScreenCardBackground,
                shape = MaterialTheme.shapes.medium
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        IconHeader(
            properties = section.iconHeaderProperties,
            modifier = section.headerModifier.fillMaxWidth(),
        )
        section.content()
    }
}

@Composable
private fun rememberSections(
    widgetConfiguration: ReversibleWidgetConfiguration,
    locationAccessState: LocationAccessState,
    showPropertyInfoDialog: (InfoDialogData) -> Unit,
    showCustomColorConfigurationDialog: (ColorPickerProperties) -> Unit,
    showRefreshIntervalConfigurationDialog: () -> Unit
): ImmutableList<Section> {
    val context: Context = LocalContext.current

    return remember {
        persistentListOf(
            Section(
                iconHeaderProperties = IconHeaderProperties(
                    iconRes = R.drawable.ic_palette_24,
                    stringRes = R.string.appearance
                ),
            ) {
                AppearanceConfiguration(
                    coloringConfig = widgetConfiguration.coloringConfig.collectAsStateWithLifecycle().value,
                    setColoringConfig = remember {
                        {
                            widgetConfiguration.coloringConfig.value = it
                        }
                    },
                    opacity = widgetConfiguration.opacity.collectAsStateWithLifecycle().value,
                    setOpacity = remember {
                        {
                            widgetConfiguration.opacity.value = it
                        }
                    },
                    fontSize = widgetConfiguration.fontSize.collectAsStateWithLifecycle().value,
                    setFontSize = remember {
                        { widgetConfiguration.fontSize.value = it }
                    },
                    showCustomColorConfigurationDialog = showCustomColorConfigurationDialog,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
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
                    stringRes = R.string.bottom_bar,
                )
            ) {
                PropertyCheckRowColumn(
                    dataList = remember {
                        WidgetBottomRowElement.entries.map {
                            PropertyConfigurationView.CheckRow.WithoutSubProperties.fromIsCheckedMap(
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
                    iconRes = com.w2sv.core.common.R.drawable.ic_refresh_24,
                    stringRes = R.string.refreshing,
                )
            ) {
                PropertyCheckRowColumn(
                    dataList = remember {
                        persistentListOf(
                            PropertyConfigurationView.CheckRow.WithSubProperties.fromIsCheckedMap(
                                property = WidgetRefreshingParameter.RefreshPeriodically,
                                isCheckedMap = widgetConfiguration.refreshingParametersMap,
                                infoDialogData = InfoDialogData(
                                    title = context.getString(WidgetRefreshingParameter.RefreshPeriodically.labelRes),
                                    description = context.getString(R.string.refresh_periodically_info)
                                ),
                                subPropertyCheckRowDataList = persistentListOf(
                                    PropertyConfigurationView.Custom {
                                        RefreshIntervalConfigurationRow(
                                            interval = widgetConfiguration.refreshInterval.collectAsStateWithLifecycle().value,
                                            showConfigurationDialog = showRefreshIntervalConfigurationDialog,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        )
                                    },
                                    PropertyConfigurationView.CheckRow.WithoutSubProperties.fromIsCheckedMap(
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
}

@Composable
private fun RefreshIntervalConfigurationRow(
    interval: Duration,
    showConfigurationDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubPropertyKeyboardArrowRightIcon()
        Text(text = stringResource(R.string.interval))
        Spacer(modifier = Modifier.weight(1f))

        Text(text = remember(interval) {
            interval.run {
                when {
                    inWholeHours == 0L -> "${minutes}m"
                    minutes == 0 -> "${inWholeHours}h"
                    else -> "${inWholeHours}h ${minutes}m"
                }
            }
        })
        IconButton(
            onClick = showConfigurationDialog,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(38.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_edit_24),
                contentDescription = stringResource(R.string.open_the_refresh_interval_configuration_dialog),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun rememberWidgetWifiPropertyCheckRowData(
    widgetConfiguration: ReversibleWidgetConfiguration,
    locationAccessState: LocationAccessState,
): ImmutableList<PropertyConfigurationView.CheckRow<WidgetWifiProperty>> {
    val context = LocalContext.current
    val snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current
    val scope: CoroutineScope = rememberCoroutineScope()

    val showLeaveAtLeastOnePropertyEnabledSnackbar: () -> Unit = remember {
        {
            scope.launch {
                snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                    AppSnackbarVisuals(
                        msg = context.getString(R.string.leave_at_least_one_property_enabled),
                        kind = SnackbarKind.Error
                    )
                )
            }
        }
    }

    return remember {
        WidgetWifiProperty.entries
            .map { property ->
                val shakeController = ShakeController(shakeConfig)

                when (property) {
                    is WidgetWifiProperty.NonIP -> {
                        PropertyConfigurationView.CheckRow.WithoutSubProperties.fromIsCheckedMap(
                            property = property,
                            isCheckedMap = widgetConfiguration.wifiProperties,
                            allowCheckChange = { isCheckedNew ->
                                if (property is WidgetWifiProperty.NonIP.LocationAccessRequiring && isCheckedNew) {
                                    return@fromIsCheckedMap locationAccessState.isGranted.also {
                                        if (!it) {
                                            locationAccessState.launchRequest(
                                                LocationAccessPermissionRequestTrigger.PropertyCheckChange(
                                                    property,
                                                )
                                            )
                                        }
                                    }
                                }
                                (isCheckedNew || widgetConfiguration.moreThanOnePropertyChecked()).also {
                                    if (!it) {
                                        showLeaveAtLeastOnePropertyEnabledSnackbar()
                                    }
                                }
                            },
                            onCheckedChangedDisallowed = { shakeController.shake() },
                            infoDialogData = property.toInfoDialogData(context),
                            modifier = Modifier.shake(shakeController)
                        )
                    }

                    is WidgetWifiProperty.IP -> {
                        PropertyConfigurationView.CheckRow.WithSubProperties.fromIsCheckedMap(
                            property = property,
                            isCheckedMap = widgetConfiguration.wifiProperties,
                            allowCheckChange = { isCheckedNew ->
                                isCheckedNew || widgetConfiguration.moreThanOnePropertyChecked()
                            },
                            onCheckedChangedDisallowed = {
                                shakeController.shake()
                                showLeaveAtLeastOnePropertyEnabledSnackbar()
                            },
                            subPropertyCheckRowDataList = property.subProperties
                                .map { subProperty ->
                                    val subPropertyShakeController =
                                        if (subProperty.isAddressTypeEnablementProperty)
                                            ShakeController(shakeConfig)
                                        else
                                            null

                                    PropertyConfigurationView.CheckRow.WithoutSubProperties.fromIsCheckedMap(
                                        property = subProperty,
                                        isCheckedMap = widgetConfiguration.ipSubProperties,
                                        allowCheckChange = { newValue ->
                                            subProperty.allowCheckedChange(
                                                newValue,
                                                widgetConfiguration.ipSubProperties
                                            )
                                        },
                                        onCheckedChangedDisallowed = {
                                            subPropertyShakeController?.shake()
                                            scope.launch {
                                                snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                                                    AppSnackbarVisuals(
                                                        msg = context.getString(R.string.leave_at_least_one_address_version_enabled),
                                                        kind = SnackbarKind.Error
                                                    )
                                                )
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
                            infoDialogData = property.toInfoDialogData(context),
                            modifier = Modifier.shake(shakeController)
                        )
                    }
                }
            }
            .toPersistentList()
    }
}

private fun ReversibleWidgetConfiguration.moreThanOnePropertyChecked(): Boolean =
    wifiProperties.values.count { it } > 1

private val shakeConfig = ShakeConfig(
    iterations = 2,
    translateX = 12.5f,
    stiffness = 20_000f
)

private fun WidgetWifiProperty.IP.SubProperty.allowCheckedChange(
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