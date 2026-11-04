package com.w2sv.wifiwidget.ui.screen.widgetconfig.model

import android.content.Context
import com.w2sv.composed.material3.SnackbarController
import com.w2sv.core.resources.R
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.WifiPropertyConfig
import com.w2sv.domain.wifiproperty.settings.IpSetting
import com.w2sv.domain.wifiproperty.settings.LocationParameter
import com.w2sv.domain.wifiproperty.settings.WifiPropertySetting
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigItem
import com.w2sv.wifiwidget.ui.designsystem.configlist.makeTrySetChecked
import com.w2sv.wifiwidget.ui.location.OnLocationAccessGranted
import com.w2sv.wifiwidget.ui.location.capability.access.LocationAccessCapability
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.screen.widgetconfig.list.UpdateWidgetConfig
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarBuilder
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun wifiPropertyConfigItem(
    property: WifiProperty,
    config: WidgetConfig,
    updateConfig: UpdateWidgetConfig,
    showDialog: (WidgetConfigDialog) -> Unit,
    locationAccess: LocationAccessCapability,
    scope: CoroutineScope,
    snackbarController: SnackbarController
): ConfigItem =
    property.configItem(
        config = { config.propertyConfig(property) },
        update = { propertyConfig -> updateConfig { withUpdatedPropertyConfig(property) { propertyConfig } } },
        locationAccess = locationAccess,
        isMoreThanOnePropertyEnabled = { config.enabledProperties.size > 1 },
        showDialog = showDialog,
        showSnackbar = { builder -> scope.launch { snackbarController.replaceCurrentWith { builder() } } }
    )

private fun WifiProperty.configItem(
    config: () -> WifiPropertyConfig<WifiPropertySetting>,
    update: (WifiPropertyConfig<WifiPropertySetting>) -> Unit,
    locationAccess: LocationAccessCapability,
    isMoreThanOnePropertyEnabled: () -> Boolean,
    showDialog: (WidgetConfigDialog) -> Unit,
    showSnackbar: (SnackbarBuilder) -> Unit
): ConfigItem =
    ConfigItem(
        property = this,
        isChecked = { config().isEnabled },
        trySetChecked = makeTrySetChecked(
            updateIsEnabled = { update(config().copy(isEnabled = it)) },
            locationAccess = locationAccess,
            isMoreThanOnePropertyEnabled = isMoreThanOnePropertyEnabled,
            onUncheckingLastEnabledProperty = { showSnackbar { leaveAtLeastOnePropertyEnabledSnackbar() } }
        ),
        showInfoDialog = { showDialog(infoDialogData()) }
    )

fun wifiPropertySubSettingItems(
    property: WifiProperty,
    config: WidgetConfig,
    updateConfig: UpdateWidgetConfig,
    scope: CoroutineScope,
    snackbarController: SnackbarController
): ImmutableList<ConfigItem>? {
    val propertyConfig = { config.propertyConfig(property) }
    val update: (WifiPropertyConfig<WifiPropertySetting>) -> Unit =
        { updated -> updateConfig { withUpdatedPropertyConfig(property) { updated } } }
    val showSnackbar: (SnackbarBuilder) -> Unit =
        { builder -> scope.launch { snackbarController.replaceCurrentWith { builder() } } }

    return when (property) {
        is WifiProperty.IpProperty -> ipSettingItems(
            settings = property.settings,
            isSettingEnabled = { propertyConfig().isSettingEnabled(it) },
            updateSetting = { setting, value -> update(propertyConfig().withUpdatedSetting(setting, value)) },
            showLeaveAtLeastOneAddressVersionEnabledSnackbar = { showSnackbar { leaveAtLeastOneAddressVersionEnabledSnackbar() } }
        )

        is WifiProperty.Location -> locationSettingItems(
            isSettingEnabled = { propertyConfig().isSettingEnabled(it) },
            isMoreThanOneParameterEnabled = { propertyConfig().enabledSettings.size > 1 },
            updateSetting = { setting, value -> update(propertyConfig().withUpdatedSetting(setting, value)) },
            showLeaveAtLeastOnePropertyEnabledSnackbar = { showSnackbar { leaveAtLeastOnePropertyEnabledSnackbar() } }
        )

        else -> null
    }
}

enum class WifiPropertyCheckError {
    LocationAccessMissing,
    UncheckingLastEnabledProperty
}

private fun WifiProperty.makeTrySetChecked(
    updateIsEnabled: (Boolean) -> Unit,
    locationAccess: LocationAccessCapability,
    isMoreThanOnePropertyEnabled: () -> Boolean,
    onUncheckingLastEnabledProperty: () -> Unit
) =
    makeTrySetChecked(
        updateVetoReason = { isCheckedNew ->
            when {
                requiresLocationAccess && isCheckedNew && !locationAccess.foregroundPermissionsGranted ->
                    WifiPropertyCheckError.LocationAccessMissing

                !isCheckedNew && !isMoreThanOnePropertyEnabled() -> WifiPropertyCheckError.UncheckingLastEnabledProperty
                else -> null
            }
        },
        onVeto = { checkError ->
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
    showLeaveAtLeastOneAddressVersionEnabledSnackbar: () -> Unit
): ImmutableList<ConfigItem> =
    settings.map { setting ->
        ConfigItem(
            property = setting,
            isChecked = { isSettingEnabled(setting) },
            trySetChecked = makeTrySetChecked(
                allowUpdate = { newValue -> allowIpSettingUpdate(setting, newValue, isSettingEnabled) },
                onUpdateDisallowed = {
                    showLeaveAtLeastOneAddressVersionEnabledSnackbar()
                },
                update = { updateSetting(setting, it) }
            ),
            show = { setting != IpSetting.ShowSubnetMask || isSettingEnabled(IpSetting.V4Enabled) }
        )
    }.toPersistentList()

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
    showLeaveAtLeastOnePropertyEnabledSnackbar: () -> Unit
): ImmutableList<ConfigItem> =
    LocationParameter.entries.map { param ->
        ConfigItem(
            property = param,
            isChecked = { isSettingEnabled(param) },
            trySetChecked = makeTrySetChecked(
                allowUpdate = { newValue -> newValue || isMoreThanOneParameterEnabled() },
                onUpdateDisallowed = {
                    showLeaveAtLeastOnePropertyEnabledSnackbar()
                },
                update = { updateSetting(param, it) }
            )
        )
    }.toPersistentList()

private fun WifiProperty.infoDialogData(): WidgetConfigDialog.Info =
    WidgetConfigDialog.Info(
        titleRes = labelRes,
        descriptionRes = descriptionRes,
        learnMoreUrl = learnMoreUrl
    )
