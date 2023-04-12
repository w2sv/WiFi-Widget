package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.configcolumn

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
import com.w2sv.common.WifiProperty
import com.w2sv.wifiwidget.ui.shared.InfoIconButton
import com.w2sv.wifiwidget.ui.shared.JostText

@Composable
internal fun PropertySelectionSection(
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

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        JostText(
            text = label,
            modifier = Modifier.weight(1f, fill = true),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        )
        Checkbox(
            checked = propertyChecked(property),
            onCheckedChange = { onCheckedChange(property, it) },
            modifier = Modifier.semantics {
                contentDescription = "Set/unset $label."
            }
        )
        InfoIconButton(
            onClick = {
                onInfoButtonClick(property)
            },
            contentDescription = "Open a ${stringResource(id = property.labelRes)} info dialog."
        )
    }
}