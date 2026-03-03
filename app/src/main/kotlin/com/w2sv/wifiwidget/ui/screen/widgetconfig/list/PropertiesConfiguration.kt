package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.composed.core.extensions.thenIf
import com.w2sv.domain.model.widget.WifiWidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.WifiPropertyConfig
import com.w2sv.domain.model.wifiproperty.settings.IpSetting
import com.w2sv.domain.model.wifiproperty.settings.LocationParameter
import com.w2sv.domain.model.wifiproperty.settings.WifiPropertySetting
import com.w2sv.kotlinutils.copy
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.LocalLocationAccessCapability
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.DropdownMenuItemProperties
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.designsystem.InfoIcon
import com.w2sv.wifiwidget.ui.designsystem.MoreIconButtonWithDropdownMenu
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.infoDialogData
import com.w2sv.wifiwidget.ui.sharedstate.location.OnLocationAccessGranted
import com.w2sv.wifiwidget.ui.sharedstate.location.access_capability.LocationAccessCapability
import com.w2sv.wifiwidget.ui.util.SnackbarBuilder
import com.w2sv.wifiwidget.ui.theme.onSurfaceVariantLowAlpha
import com.w2sv.wifiwidget.ui.util.ShakeController
import com.w2sv.wifiwidget.ui.util.SnackbarEmitter
import com.w2sv.wifiwidget.ui.util.WithLocalContentColor
import com.w2sv.wifiwidget.ui.util.rememberSnackbarEmitter
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
            iconRes = R.drawable.ic_checklist_24,
            stringRes = R.string.properties,
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
        PropertyReorderingDisclaimer(
            modifier = Modifier
                .fillMaxWidth()
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
                snackbarEmitter = rememberSnackbarEmitter()
            ),
            onDrop = { fromIndex: Int, toIndex: Int ->
                updateConfig { withModifiedPropertyPosition(fromIndex, toIndex) }
            }
        )
    }
}

@Composable
private fun PropertyReorderingDisclaimer(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WithLocalContentColor(MaterialTheme.colorScheme.onSurfaceVariantLowAlpha) {
            InfoIcon()
            Text(stringResource(R.string.wifi_property_reordering_information), fontSize = 13.sp)
        }
    }
}

private fun wifiPropertyCheckRowData(
    config: WifiWidgetConfig,
    updateConfig: UpdateWidgetConfig,
    showDialog: (WidgetConfigDialog) -> Unit,
    locationAccess: LocationAccessCapability,
    scope: CoroutineScope,
    snackbarEmitter: SnackbarEmitter
): ImmutableList<ConfigListElement.CheckRow<WifiProperty>> =
    config.orderedProperties
        .map { property ->
            property.checkRow(
                config = { config.properties.getValue(property) },
                update = { propertyConfig -> updateConfig { copy(properties = properties.copy { put(property, propertyConfig) }) } },
                locationAccess = locationAccess,
                isMoreThanOnePropertyEnabled = { config.orderedProperties.count { config.isEnabled(it) } > 1 },
                showDialog = showDialog,
                showSnackbar = { snackbarEmitter.dismissCurrentAndShow(scope) { it(this) } },
                scope = scope
            )
        }
        .toPersistentList()

private fun Context.leaveAtLeastOnePropertyEnabledSnackbar() = AppSnackbarVisuals(
    msg = getString(R.string.leave_at_least_one_property_enabled),
    kind = SnackbarKind.Warning
)

private fun Context.leaveAtLeastOneAddressVersionEnabledSnackbar() = AppSnackbarVisuals(
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
): ConfigListElement.CheckRow<WifiProperty> {
    val shakeController = ShakeController()

    return ConfigListElement.CheckRow(
        property = this,
        isChecked = { config().isEnabled },
        onCheckedChange = makeOnCheckedChange(
            allowCheckChange = { isCheckedNew ->
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
            onCheckedChangedDisallowed = { scope.launch { shakeController.shake() } },
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
    val shakeController = ShakeController()
    return buildList {
        if (settings.any { it.isVersionSetting }) {
            add(
                ConfigListElement.Custom {
                    VersionsHeader(modifier = Modifier.padding(top = SubPropertyColumnDefaults.startPadding))
                }
            )
        }
        settings.map { setting ->
            val checkRow = ConfigListElement.CheckRow(
                property = setting,
                isChecked = { isSettingEnabled(setting) },
                onCheckedChange = makeOnCheckedChange(
                    allowCheckChange = { newValue -> allowIpSettingUpdate(setting, newValue, isSettingEnabled) },
                    onCheckedChangedDisallowed = {
                        scope.launch { shakeController.shake() }
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
                        onTrue = { padding(start = 16.dp) }
                    )
            )
            add(checkRow)
        }
    }
        .toPersistentList()
}

private val IpSetting.isVersionSetting: Boolean
    get() = this in listOf(IpSetting.V4Enabled, IpSetting.V6Enabled)

private fun allowIpSettingUpdate(
    setting: IpSetting,
    newValue: Boolean,
    isSettingEnabled: (IpSetting) -> Boolean,
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
): ImmutableList<ConfigListElement> = LocationParameter.entries.map { param ->
    val shakeController = ShakeController()
    ConfigListElement.CheckRow(
        property = param,
        isChecked = { isSettingEnabled(param) },
        onCheckedChange = makeOnCheckedChange(
            allowCheckChange = { newValue ->
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
