package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.extensions.thenIf
import com.w2sv.core.common.R
import com.w2sv.domain.model.widget.WifiWidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.WifiPropertyConfig
import com.w2sv.domain.model.wifiproperty.settings.IpSetting
import com.w2sv.domain.model.wifiproperty.settings.LocationParameter
import com.w2sv.domain.model.wifiproperty.settings.WifiPropertySetting
import com.w2sv.kotlinutils.copy
import com.w2sv.kotlinutils.makeIf
import com.w2sv.wifiwidget.ui.LocalLocationAccessCapability
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.DisclaimerRow
import com.w2sv.wifiwidget.ui.designsystem.DropdownMenuItemProperties
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.designsystem.MoreIconButtonWithDropdownMenu
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.screen.widgetconfig.model.infoDialogData
import com.w2sv.wifiwidget.ui.sharedstate.location.OnLocationAccessGranted
import com.w2sv.wifiwidget.ui.sharedstate.location.access_capability.LocationAccessCapability
import com.w2sv.wifiwidget.ui.util.PreviewOf
import com.w2sv.wifiwidget.ui.util.ShakeController
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarBuilder
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarController
import com.w2sv.wifiwidget.ui.util.snackbar.rememberSnackbarController
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun WifiPropertiesConfigCard(
    config: WifiWidgetConfig,
    updateConfig: UpdateWidgetConfig,
    showDialog: (WidgetConfigDialog) -> Unit
) {
    WidgetConfigSectionCard(
        IconHeader(
            iconRes = R.drawable.ic_wifi_24,
            stringRes = R.string.wifi_properties,
            trailingIcon = {
                MoreIconButtonWithDropdownMenu(
                    menuItems = remember {
                        persistentListOf(
                            DropdownMenuItemProperties(
                                R.string.restore_default_order,
                                onClick = { updateConfig { withDefaultPropertyOrder() } },
                                enabled = { !config.propertiesInDefaultOrder },
                                leadingIconRes = R.drawable.ic_restart_alt_24
                            )
                        )
                    }
                )
            }
        )
    ) {
        DisclaimerRow(
            text = stringResource(R.string.wifi_property_reordering_information),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 14.dp)
                .padding(bottom = 8.dp)
        )
        DragAndDroppableCheckRowColumn(
            elements = wifiPropertyCheckRowData(
                config = config,
                updateConfig = updateConfig,
                showDialog = showDialog,
                locationAccess = LocalLocationAccessCapability.current,
                scope = rememberCoroutineScope(),
                snackbarController = rememberSnackbarController()
            ),
            onDrop = { fromIndex: Int, toIndex: Int ->
                updateConfig { withModifiedPropertyPosition(fromIndex, toIndex) }
            }
        )
    }
}

@Preview
@Composable
private fun Prev() {
    PreviewOf {
        WifiPropertiesConfigCard(
            WifiWidgetConfig.default,
            {},
            {}
        )
    }
}

private fun wifiPropertyCheckRowData(
    config: WifiWidgetConfig,
    updateConfig: UpdateWidgetConfig,
    showDialog: (WidgetConfigDialog) -> Unit,
    locationAccess: LocationAccessCapability,
    scope: CoroutineScope,
    snackbarController: SnackbarController
): ImmutableList<ConfigListElement.CheckRow> =
    config.orderedProperties
        .map { property ->
            property.checkRow(
                config = { config.properties.getValue(property) },
                update = { propertyConfig -> updateConfig { copy(properties = properties.copy { put(property, propertyConfig) }) } },
                locationAccess = locationAccess,
                isMoreThanOnePropertyEnabled = { config.orderedProperties.count { config.isEnabled(it) } > 1 },
                showDialog = showDialog,
                showSnackbar = { builder -> scope.launch { snackbarController.showReplacing { builder() } } },
                scope = scope
            )
        }
        .toPersistentList()

private fun Context.leaveAtLeastOnePropertyEnabledSnackbar() =
    AppSnackbarVisuals(
        msg = getString(R.string.leave_at_least_one_property_enabled),
        kind = SnackbarKind.Warning
    )

private fun Context.leaveAtLeastOneAddressVersionEnabledSnackbar() =
    AppSnackbarVisuals(
        msg = getString(R.string.leave_at_least_one_address_version_enabled),
        kind = SnackbarKind.Warning
    )

private fun WifiProperty.checkRow(
    config: () -> WifiPropertyConfig<WifiPropertySetting>,
    update: (WifiPropertyConfig<WifiPropertySetting>) -> Unit,
    locationAccess: LocationAccessCapability,
    isMoreThanOnePropertyEnabled: () -> Boolean,
    showDialog: (WidgetConfigDialog) -> Unit,
    showSnackbar: (SnackbarBuilder) -> Unit,
    scope: CoroutineScope
): ConfigListElement.CheckRow {
    val shakeController = ShakeController()

    return ConfigListElement.CheckRow(
        property = this,
        isChecked = { config().isEnabled },
        onCheckedChange = makeOnCheckedChange(
            allow = { isCheckedNew ->
                when {
                    requiresLocationAccess && isCheckedNew -> locationAccess.foregroundPermissionsGranted.also {
                        if (!it) {
                            locationAccess.requestPermission(OnLocationAccessGranted.EnableProperty(this))
                        }
                    }

                    else -> (isCheckedNew || isMoreThanOnePropertyEnabled()).also {
                        if (!it) {
                            showSnackbar { leaveAtLeastOnePropertyEnabledSnackbar() }
                        }
                    }
                }
            },
            onDisallowed = { scope.launch { shakeController.shake() } },
            update = { update(config().copy(isEnabled = it)) }
        ),
        shakeController = shakeController,
        subPropertyColumnElements = when (this) {
            is WifiProperty.IpProperty -> {
                ipSettingConfigEntries(
                    settings = settings,
                    isSettingEnabled = { config().settings.getValue(it) },
                    updateSetting = { setting, value -> update(config().copy(settings = config().settings.copy { put(setting, value) })) },
                    showLeaveAtLeastOneAddressVersionEnabledSnackbar = { showSnackbar { leaveAtLeastOneAddressVersionEnabledSnackbar() } },
                    scope = scope
                )
            }

            is WifiProperty.Location -> locationSettingConfigEntries(
                isSettingEnabled = { config().settings.getValue(it) },
                isMoreThanOnePropertyEnabled = { config().settings.moreThanOnePropertyEnabled() },
                updateSetting = { setting, value -> update(config().copy(settings = config().settings.copy { put(setting, value) })) },
                showLeaveAtLeastOnePropertyEnabledSnackbar = { showSnackbar { leaveAtLeastOnePropertyEnabledSnackbar() } },
                scope = scope
            )

            else -> null
        },
        showInfoDialog = { showDialog(infoDialogData()) }
    )
}

private fun Map<*, Boolean>.moreThanOnePropertyEnabled(): Boolean =
    values.count { it } > 1

private fun ipSettingConfigEntries(
    settings: List<IpSetting>,
    isSettingEnabled: (IpSetting) -> Boolean,
    updateSetting: (IpSetting, Boolean) -> Unit,
    showLeaveAtLeastOneAddressVersionEnabledSnackbar: () -> Unit,
    scope: CoroutineScope
): ImmutableList<ConfigListElement> {
    return buildList {
        if (settings.any { it.isVersionSetting }) {
            add(
                ConfigListElement.Custom {
                    VersionsHeader(modifier = Modifier.padding(top = SubPropertyColumnDefaults.startPadding))
                }
            )
        }
        settings.map { setting ->
            val shakeController = makeIf(setting.isVersionSetting) { ShakeController() }
            val checkRow = ConfigListElement.CheckRow(
                property = setting,
                isChecked = { isSettingEnabled(setting) },
                onCheckedChange = makeOnCheckedChange(
                    allow = { newValue -> allowIpSettingUpdate(setting, newValue, isSettingEnabled) },
                    onDisallowed = {
                        scope.launch { shakeController?.shake() }
                        showLeaveAtLeastOneAddressVersionEnabledSnackbar()
                    },
                    update = { updateSetting(setting, it) }
                ),
                show = {
                    if (setting == IpSetting.ShowSubnetMask) {
                        isSettingEnabled(IpSetting.V4Enabled)
                    } else {
                        true
                    }
                },
                shakeController = shakeController,
                modifier = Modifier
                    .thenIf(
                        condition = setting.isVersionSetting,
                        onTrue = { padding(start = 24.dp) }
                    )
            )
            add(checkRow)
        }
    }
        .toPersistentList()
}

@Composable
private fun VersionsHeader(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.displayed_versions),
        fontSize = SubPropertyColumnDefaults.fontSize,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

private val IpSetting.isVersionSetting: Boolean
    get() = this in listOf(IpSetting.V4Enabled, IpSetting.V6Enabled)

private fun allowIpSettingUpdate(
    setting: IpSetting,
    newValue: Boolean,
    isSettingEnabled: (IpSetting) -> Boolean
): Boolean =
    when {
        newValue -> true
        setting == IpSetting.V6Enabled -> isSettingEnabled(IpSetting.V4Enabled)
        setting == IpSetting.V4Enabled -> isSettingEnabled(IpSetting.V6Enabled)
        else -> true
    }

private fun locationSettingConfigEntries(
    isSettingEnabled: (LocationParameter) -> Boolean,
    isMoreThanOnePropertyEnabled: () -> Boolean,
    updateSetting: (LocationParameter, Boolean) -> Unit,
    showLeaveAtLeastOnePropertyEnabledSnackbar: () -> Unit,
    scope: CoroutineScope
): ImmutableList<ConfigListElement> =
    LocationParameter.entries.map { param ->
        val shakeController = ShakeController()
        ConfigListElement.CheckRow(
            property = param,
            isChecked = { isSettingEnabled(param) },
            onCheckedChange = makeOnCheckedChange(
                allow = { newValue ->
                    (newValue || isMoreThanOnePropertyEnabled()).also {
                        if (!it) {
                            scope.launch { shakeController.shake() }
                            showLeaveAtLeastOnePropertyEnabledSnackbar()
                        }
                    }
                },
                update = { updateSetting(param, it) }
            ),
            shakeController = shakeController
        )
    }
        .toPersistentList()
