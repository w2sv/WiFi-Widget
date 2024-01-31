package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.UnconfirmedWidgetConfiguration
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.getInfoDialogData
import com.w2sv.wifiwidget.ui.utils.ShakeConfig
import com.w2sv.wifiwidget.ui.utils.ShakeController
import com.w2sv.wifiwidget.ui.utils.shake
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

@Composable
fun rememberWidgetWifiPropertyCheckRowData(
    widgetConfiguration: UnconfirmedWidgetConfiguration,
    locationAccessState: LocationAccessState,
): ImmutableList<PropertyCheckRowData<WidgetWifiProperty>> {
    val context = LocalContext.current
    return remember {
        WidgetWifiProperty.entries
            .map { property ->
                val shakeController = ShakeController(shakeConfig)

                PropertyCheckRowData.fromMutableMap(
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
                    subPropertyCheckRowData = when (property) {
                        is WidgetWifiProperty.IP -> {
                            property.subProperties
                                .map { subProperty ->
                                    val subPropertyShakeController =
                                        if (subProperty.isAddressTypeEnablementProperty)
                                            ShakeController(shakeConfig)
                                        else
                                            null

                                    PropertyCheckRowData.fromMutableMap(
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
                                .toPersistentList()
                        }

                        else -> null
                    },
                    infoDialogData = property.getInfoDialogData(context),
                    modifier = Modifier.shake(shakeController)
                )
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