package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.ui.components.AppFontText
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.PropertyCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.SubPropertyCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.IPPropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyInfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.WifiPropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.infoDialogData

@Composable
internal fun WifiPropertySelection(
    wifiPropertiesMap: MutableMap<WidgetWifiProperty, Boolean>,
    ipSubPropertiesMap: MutableMap<WidgetWifiProperty.IPProperty.SubProperty, Boolean>,
    allowLAPDependentPropertyCheckChange: (WidgetWifiProperty.LocationAccessPermissionRequiring, Boolean) -> Boolean,
    showInfoDialog: (PropertyInfoDialogData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val indexToFirstSubTypeTitle = remember {
        mapOf(
            0 to "Location access requiring",
            WidgetWifiProperty.LocationAccessPermissionRequiring.entries.size to "IP Addresses",
            WidgetWifiProperty.LocationAccessPermissionRequiring.entries.size + WidgetWifiProperty.IPProperty.entries.size to "Other"
        )
    }

    Column(modifier = modifier) {
        remember {
            WidgetWifiProperty.entries.map { property ->
                when (property) {
                    is WidgetWifiProperty.LocationAccessPermissionRequiring -> WifiPropertyCheckRowData(
                        property = property,
                        isCheckedMap = wifiPropertiesMap,
                        allowCheckChange = {
                            allowLAPDependentPropertyCheckChange(
                                property,
                                it,
                            )
                        },
                    )

                    is WidgetWifiProperty.IPProperty -> {
                        IPPropertyCheckRowData(
                            property = property,
                            isCheckedMap = wifiPropertiesMap,
                            subPropertyIsCheckedMap = ipSubPropertiesMap,
                        )
                    }

                    else -> WifiPropertyCheckRowData(
                        property = property,
                        isCheckedMap = wifiPropertiesMap,
                    )
                }
            }
        }
            .forEachIndexed { index, data ->
                indexToFirstSubTypeTitle[index]?.let { subTypeTitle ->
                    AppFontText(
                        text = subTypeTitle,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                WifiPropertyCheckRow(
                    data = data,
                    showInfoDialog = showInfoDialog,
                )
            }
    }
}

@Composable
private fun WifiPropertyCheckRow(
    data: WifiPropertyCheckRowData,
    showInfoDialog: (PropertyInfoDialogData) -> Unit,
) {
    Column {
        PropertyCheckRow(
            data = data,
            onInfoButtonClick = { showInfoDialog(data.property.viewData.infoDialogData) },
        )
        if (data is IPPropertyCheckRowData) {
            AnimatedVisibility(visible = data.isChecked()) {
                IPSubPropertyCheckRows(
                    subProperties = (data.property as WidgetWifiProperty.IPProperty).subProperties,
                    subPropertyIsCheckedMap = data.subPropertyIsCheckedMap,
                )
            }
        }
    }
}

@Composable
private fun IPSubPropertyCheckRows(
    subProperties: List<WidgetWifiProperty.IPProperty.SubProperty>,
    subPropertyIsCheckedMap: MutableMap<WidgetWifiProperty.IPProperty.SubProperty, Boolean>,
) {
    Column {
        subProperties.forEach { subProperty ->
            SubPropertyCheckRow(
                data = PropertyCheckRowData(
                    type = subProperty,
                    labelRes = subProperty.kind.labelRes,
                    isCheckedMap = subPropertyIsCheckedMap,
                    allowCheckChange = { newValue ->
                        !(
                                !newValue
                                        && subProperty.isAddressTypeEnablementProperty
                                        && subPropertyIsCheckedMap.count { (k, v) -> k.isAddressTypeEnablementProperty && v } == 1)
                    },
                ),
            )
        }
    }
}
