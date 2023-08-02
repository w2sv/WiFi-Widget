package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.bulletPointText

@Stable
internal data class ParameterSelection<T>(
    val label: String,
    val type: T
)

@Composable
internal fun <T> ParameterCheckRow(
    data: ParameterSelection<T>,
    typeToIsChecked: MutableMap<T, Boolean>,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified
) {
    val checkBoxCD = stringResource(id = R.string.set_unset).format(data.label)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        JostText(text = bulletPointText(data.label), fontSize = fontSize)
        Checkbox(
            checked = typeToIsChecked.getValue(data.type),
            onCheckedChange = {
                typeToIsChecked[data.type] = it
            },
            modifier = Modifier.semantics {
                contentDescription = checkBoxCD
            }
        )
    }
}