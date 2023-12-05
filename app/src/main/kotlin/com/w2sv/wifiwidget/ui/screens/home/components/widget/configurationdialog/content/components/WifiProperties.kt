package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppFontText
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.PropertyCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.SubPropertyCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.IPPropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyInfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.WifiPropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.infoDialogData

private val firstIndexToSubTypeTitleResId = mapOf(
    0 to R.string.location_access_requiring,
    WidgetWifiProperty.LocationAccessRequiring.entries.size to R.string.ip_addresses,
    WidgetWifiProperty.LocationAccessRequiring.entries.size + WidgetWifiProperty.IP.entries.size to R.string.other
)

@Composable
internal fun WifiPropertySelection(
    wifiPropertiesMap: MutableMap<WidgetWifiProperty, Boolean>,
    ipSubPropertiesMap: MutableMap<WidgetWifiProperty.IP.SubProperty, Boolean>,
    allowLAPDependentPropertyCheckChange: (WidgetWifiProperty.LocationAccessRequiring, Boolean) -> Boolean,
    showInfoDialog: (PropertyInfoDialogData) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        val propertyCheckRowData = remember {
            WidgetWifiProperty.entries.map { property ->
                when (property) {
                    is WidgetWifiProperty.LocationAccessRequiring -> WifiPropertyCheckRowData(
                        property = property,
                        isCheckedMap = wifiPropertiesMap,
                        allowCheckChange = {
                            allowLAPDependentPropertyCheckChange(
                                property,
                                it,
                            )
                        },
                    )

                    is WidgetWifiProperty.IP -> {
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

        propertyCheckRowData.forEachIndexed { index, data ->
            firstIndexToSubTypeTitleResId[index]?.let { resId ->
                PropertySubTypeHeader(
                    title = stringResource(id = resId),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
                )
            }
            WifiPropertyCheckRow(
                data = data,
                showInfoDialog = showInfoDialog,
            )
        }
    }
}

@Composable
private fun PropertySubTypeHeader(title: String, modifier: Modifier = Modifier) {
    AppFontText(
        text = title,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
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
                val subPropertyCheckRowData = remember {
                    (data.property as WidgetWifiProperty.IP).subProperties
                        .map { subProperty ->
                            PropertyCheckRowData(
                                type = subProperty,
                                labelRes = subProperty.kind.labelRes,
                                isCheckedMap = data.subPropertyIsCheckedMap,
                                allowCheckChange = { newValue ->
                                    newValue ||
                                            !(subProperty.isAddressTypeEnablementProperty)
                                },
                            )
                        }
                }
                Column {
                    subPropertyCheckRowData.forEach {
                        SubPropertyCheckRow(data = it)
                    }
                }
            }
        }
    }
}