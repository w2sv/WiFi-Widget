package com.w2sv.wifiwidget.ui.home.configurationdialog.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.sp
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.shared.InfoIconButton
import com.w2sv.wifiwidget.ui.shared.JostText

@Composable
internal fun PropertySelectionSection(
    modifier: Modifier = Modifier,
    propertyChecked: (String) -> Boolean,
    onCheckedChange: (String, Boolean) -> Unit,
    onInfoButtonClick: (Int) -> Unit
) {
    Column(modifier = modifier) {
        stringArrayResource(id = R.array.wifi_properties)
            .forEachIndexed { propertyIndex, property ->
                PropertyRow(
                    property = property,
                    propertyIndex = propertyIndex,
                    propertyChecked = propertyChecked,
                    onCheckedChange = onCheckedChange,
                    onInfoButtonClick = onInfoButtonClick
                )
            }
    }
}

@Composable
private fun PropertyRow(
    property: String,
    propertyIndex: Int,
    propertyChecked: (String) -> Boolean,
    onCheckedChange: (String, Boolean) -> Unit,
    onInfoButtonClick: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        JostText(
            text = property,
            modifier = Modifier.weight(1f, fill = true),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        )
        Checkbox(
            checked = propertyChecked(property),
            onCheckedChange = { onCheckedChange(property, it) }
        )
        InfoIconButton {
            onInfoButtonClick(propertyIndex)
        }
    }
}