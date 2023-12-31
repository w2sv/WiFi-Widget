package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.utils.bulletPointText
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.InfoIcon
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyInfoDialogData
import com.w2sv.wifiwidget.ui.utils.conditional
import kotlinx.collections.immutable.ImmutableList

@Composable
fun PropertyCheckRows(
    dataList: ImmutableList<PropertyCheckRowData<*>>,
    modifier: Modifier = Modifier,
    showInfoDialog: ((PropertyInfoDialogData) -> Unit)? = null
) {
    Column(modifier = modifier) {
        dataList
            .forEach { data ->
                // Display PropertySubTypeHeader if applicable
                propertyToSubTitleResId[data.property as? WidgetWifiProperty]?.let { resId ->
                    PropertySubTypeHeader(
                        title = stringResource(id = resId),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 8.dp)
                    )
                }
                PropertyCheckRow(
                    data = data,
                    showInfoDialog = showInfoDialog,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                        )
                    }
                )

                // Display subPropertyCheckRowData if present and property checked
                if (data.subPropertyCheckRowDataList.isNotEmpty()) {
                    AnimatedVisibility(visible = data.isChecked()) {
                        SubPropertyCheckRowColumn(
                            dataList = data.subPropertyCheckRowDataList,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
    }
}

@Composable
private fun SubPropertyCheckRowColumn(
    dataList: ImmutableList<PropertyCheckRowData<*>>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        dataList.forEach { checkRowData ->
            if ((checkRowData.property as? WidgetWifiProperty.IP.SubProperty)?.kind is WidgetWifiProperty.IP.V4AndV6.AddressTypeEnablement.V4Enabled) {
                Text(
                    text = stringResource(R.string.versions),
                    modifier = Modifier.padding(vertical = 2.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = subPropertyTextColor,
                )
            }
            PropertyCheckRow(
                data = checkRowData,
                modifier = Modifier.conditional(
                    condition = (checkRowData.property as? WidgetWifiProperty.IP.SubProperty)?.isAddressTypeEnablementProperty == true,
                    onTrue = { padding(start = 16.dp) }
                ),
                fontSize = 14.sp,
                makeText = ::bulletPointText,
                textColor = subPropertyTextColor
            )
        }
    }
}

private val subPropertyTextColor
    @Composable
    get() = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)

private val propertyToSubTitleResId = mapOf(
    WidgetWifiProperty.NonIP.LocationAccessRequiring.entries.first() to R.string.location_access_requiring,
    WidgetWifiProperty.IP.entries.first() to R.string.ip_addresses,
    WidgetWifiProperty.NonIP.Other.entries.first() to R.string.other
)

@Composable
private fun PropertySubTypeHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

@Composable
private fun PropertyCheckRow(
    data: PropertyCheckRowData<*>,
    modifier: Modifier = Modifier,
    makeText: (String) -> String = { it },
    fontSize: TextUnit = TextUnit.Unspecified,
    textColor: Color = Color.Unspecified,
    leadingIcon: (@Composable () -> Unit)? = null,
    showInfoDialog: ((PropertyInfoDialogData) -> Unit)? = null,
) {
    val label = stringResource(id = data.property.labelRes)
    val checkBoxCD = stringResource(id = R.string.set_unset, label)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .then(data.modifier),
    ) {

        leadingIcon?.invoke()
        Text(
            text = makeText(label),
            fontSize = fontSize,
            modifier = Modifier.weight(1.0f, true),
            color = textColor
        )
        Checkbox(
            checked = data.isChecked(),
            onCheckedChange = {
                if (data.allowCheckChange(it)) {
                    data.onCheckedChange(it)
                }
            },
            modifier = Modifier.semantics {
                contentDescription = checkBoxCD
            },
        )
        if (showInfoDialog != null && data.infoDialogData != null) {
            InfoIconButton(
                onClick = { showInfoDialog(data.infoDialogData) },
                contentDescription = stringResource(id = R.string.info_icon_cd, label),
            )
        }
    }
}

@Composable
private fun InfoIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        InfoIcon(
            contentDescription = contentDescription,
            modifier = Modifier.size(
                dimensionResource(id = R.dimen.size_icon),
            ),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
