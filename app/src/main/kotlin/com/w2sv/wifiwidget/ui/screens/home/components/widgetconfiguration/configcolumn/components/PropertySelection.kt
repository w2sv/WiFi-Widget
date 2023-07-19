package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import com.w2sv.common.enums.WifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.InfoIconButton
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.bulletPointText

@Composable
internal fun PropertySelection(
    modifier: Modifier = Modifier,
    propertyChecked: (WifiProperty) -> Boolean,
    onCheckedChange: (WifiProperty, Boolean) -> Unit,
    onInfoButtonClick: (WifiProperty) -> Unit
) {
    Column(modifier = modifier) {
        WifiProperty.values().forEach {
            PropertyRow(
                property = it,
                propertyChecked = propertyChecked,
                onCheckedChange = onCheckedChange,
                onInfoButtonClick = onInfoButtonClick
            )
        }
    }
}

@Composable
private fun PropertyRow(
    property: WifiProperty,
    propertyChecked: (WifiProperty) -> Boolean,
    onCheckedChange: (WifiProperty, Boolean) -> Unit,
    onInfoButtonClick: (WifiProperty) -> Unit
) {
    val label = stringResource(id = property.labelRes)
    val checkBoxCD = stringResource(id = R.string.set_unset).format(label)
    val infoIconCD = stringResource(id = R.string.info_icon_cd).format(label)

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        JostText(
            text = bulletPointText(label),
            modifier = Modifier.weight(1f, fill = true),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        )
        Checkbox(
            checked = propertyChecked(property),
            onCheckedChange = { onCheckedChange(property, it) },
            modifier = Modifier.semantics {
                contentDescription = checkBoxCD
            }
        )
        InfoIconButton(
            onClick = {
                onInfoButtonClick(property)
            },
            contentDescription = infoIconCD
        )
    }
}