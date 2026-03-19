package com.w2sv.wifiwidget.ui.screen.widgetconfig.model

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.w2sv.composed.core.extensions.thenIf
import com.w2sv.core.common.R
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.WifiPropertyConfig
import com.w2sv.domain.model.wifiproperty.settings.IpSetting
import com.w2sv.domain.model.wifiproperty.settings.LocationParameter
import com.w2sv.domain.model.wifiproperty.settings.WifiPropertySetting
import com.w2sv.kotlinutils.makeIf
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigItem
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigListToken
import com.w2sv.wifiwidget.ui.designsystem.configlist.makeOnCheckedChange
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.screen.widgetconfig.list.IpVersionsHeader
import com.w2sv.wifiwidget.ui.screen.widgetconfig.list.UpdateWidgetConfig
import com.w2sv.wifiwidget.ui.sharedstate.location.OnLocationAccessGranted
import com.w2sv.wifiwidget.ui.sharedstate.location.access_capability.LocationAccessCapability
import com.w2sv.wifiwidget.ui.util.ShakeController
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarBuilder
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarController
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun wifiPropertyConfigItems(
    config: WidgetConfig,
    updateConfig: UpdateWidgetConfig,
    showDialog: (WidgetConfigDialog) -> Unit,
    locationAccess: LocationAccessCapability,
    scope: CoroutineScope,
    snackbarController: SnackbarController
): ImmutableList<ConfigItem.Checkable> =
    config
        .supportedProperties
        .map { property ->
            property.configItem(
                config = { config.propertyConfig(property) },
                update = { propertyConfig -> updateConfig { withUpdatedPropertyConfig(property) { propertyConfig } } },
                locationAccess = locationAccess,
                isMoreThanOnePropertyEnabled = { config.enabledProperties.size > 1 },
                showDialog = showDialog,
                showSnackbar = { builder -> scope.launch { snackbarController.showReplacing { builder() } } },
                scope = scope
            )
        }
        .toPersistentList()

private fun WifiProperty.configItem(
    config: () -> WifiPropertyConfig<WifiPropertySetting>,
    update: (WifiPropertyConfig<WifiPropertySetting>) -> Unit,
    locationAccess: LocationAccessCapability,
    isMoreThanOnePropertyEnabled: () -> Boolean,
    showDialog: (WidgetConfigDialog) -> Unit,
    showSnackbar: (SnackbarBuilder) -> Unit,
    scope: CoroutineScope
): ConfigItem.Checkable {
    val shakeController = ShakeController()

    return ConfigItem.Checkable(
        property = this,
        isChecked = { config().isEnabled },
        onCheckedChange = makeOnCheckedChange(
            updateIsEnabled = { update(config().copy(isEnabled = it)) },
            locationAccess = locationAccess,
            isMoreThanOnePropertyEnabled = isMoreThanOnePropertyEnabled,
            onUncheckingLastEnabledProperty = { showSnackbar { leaveAtLeastOnePropertyEnabledSnackbar() } },
            shake = { scope.launch { shakeController.shake() } }
        ),
        shakeController = shakeController,
        contentBeneath = when (this) {
            is WifiProperty.IpProperty -> ConfigItem.SubSettings(
                elements = ipSettingItems(
                    settings = settings,
                    isSettingEnabled = { config().isSettingEnabled(it) },
                    updateSetting = { setting, value -> update(config().withUpdatedSetting(setting, value)) },
                    showLeaveAtLeastOneAddressVersionEnabledSnackbar = { showSnackbar { leaveAtLeastOneAddressVersionEnabledSnackbar() } },
                    scope = scope
                )
            )

            is WifiProperty.Location -> ConfigItem.SubSettings(
                elements = locationSettingItems(
                    isSettingEnabled = { config().isSettingEnabled(it) },
                    isMoreThanOneParameterEnabled = { config().enabledSettings.size > 1 },
                    updateSetting = { setting, value -> update(config().withUpdatedSetting(setting, value)) },
                    showLeaveAtLeastOnePropertyEnabledSnackbar = { showSnackbar { leaveAtLeastOnePropertyEnabledSnackbar() } },
                    scope = scope
                )
            )

            else -> null
        },
        showInfoDialog = { showDialog(infoDialogData()) }
    )
}

enum class WifiPropertyCheckError {
    LocationAccessMissing,
    UncheckingLastEnabledProperty
}

private fun WifiProperty.makeOnCheckedChange(
    updateIsEnabled: (Boolean) -> Unit,
    locationAccess: LocationAccessCapability,
    isMoreThanOnePropertyEnabled: () -> Boolean,
    onUncheckingLastEnabledProperty: () -> Unit,
    shake: () -> Unit
) =
    makeOnCheckedChange(
        updateVetoReason = { isCheckedNew ->
            when {
                requiresLocationAccess && isCheckedNew && !locationAccess.foregroundPermissionsGranted -> WifiPropertyCheckError.LocationAccessMissing
                !isCheckedNew && !isMoreThanOnePropertyEnabled() -> WifiPropertyCheckError.UncheckingLastEnabledProperty
                else -> null
            }
        },
        onVeto = { checkError ->
            shake()
            when (checkError) {
                WifiPropertyCheckError.LocationAccessMissing -> locationAccess.requestPermission(
                    OnLocationAccessGranted.EnableProperty(this)
                )
                WifiPropertyCheckError.UncheckingLastEnabledProperty -> onUncheckingLastEnabledProperty()
            }
        },
        update = updateIsEnabled
    )

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

private fun ipSettingItems(
    settings: List<IpSetting>,
    isSettingEnabled: (IpSetting) -> Boolean,
    updateSetting: (IpSetting, Boolean) -> Unit,
    showLeaveAtLeastOneAddressVersionEnabledSnackbar: () -> Unit,
    scope: CoroutineScope
): ImmutableList<ConfigItem> =
    buildList {
        if (settings.any { it.isVersionSetting }) {
            add(
                ConfigItem.Custom { IpVersionsHeader(Modifier.padding(top = ConfigListToken.itemSpacing)) }
            )
        }
        addAll(
            settings.map { setting ->
                val shakeController = makeIf(setting.isVersionSetting) { ShakeController() }
                ConfigItem.Checkable(
                    property = setting,
                    isChecked = { isSettingEnabled(setting) },
                    onCheckedChange = makeOnCheckedChange(
                        allowUpdate = { newValue -> allowIpSettingUpdate(setting, newValue, isSettingEnabled) },
                        onUpdateDisallowed = {
                            scope.launch { shakeController?.shake() }
                            showLeaveAtLeastOneAddressVersionEnabledSnackbar()
                        },
                        update = { updateSetting(setting, it) }
                    ),
                    show = { setting != IpSetting.ShowSubnetMask || isSettingEnabled(IpSetting.V4Enabled) },
                    shakeController = shakeController,
                    modifier = Modifier
                        .thenIf(
                            condition = setting.isVersionSetting,
                            onTrue = { padding(start = ConfigListToken.startPaddingSecondLevelSubSettings) }
                        )
                )
            }
        )
    }
        .toPersistentList()

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

private fun locationSettingItems(
    isSettingEnabled: (LocationParameter) -> Boolean,
    isMoreThanOneParameterEnabled: () -> Boolean,
    updateSetting: (LocationParameter, Boolean) -> Unit,
    showLeaveAtLeastOnePropertyEnabledSnackbar: () -> Unit,
    scope: CoroutineScope
): ImmutableList<ConfigItem> =
    LocationParameter.entries.map { param ->
        val shakeController = ShakeController()
        ConfigItem.Checkable(
            property = param,
            isChecked = { isSettingEnabled(param) },
            onCheckedChange = makeOnCheckedChange(
                allowUpdate = { newValue -> newValue || isMoreThanOneParameterEnabled() },
                onUpdateDisallowed = {
                    scope.launch { shakeController.shake() }
                    showLeaveAtLeastOnePropertyEnabledSnackbar()
                },
                update = { updateSetting(param, it) }
            ),
            shakeController = shakeController
        )
    }
        .toPersistentList()

private fun WifiProperty.infoDialogData(): WidgetConfigDialog.Info =
    WidgetConfigDialog.Info(
        titleRes = labelRes,
        descriptionRes = descriptionRes,
        learnMoreUrl = learnMoreUrl
    )
