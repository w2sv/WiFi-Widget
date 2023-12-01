package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.utils.bulletPointText
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppFontText
import com.w2sv.wifiwidget.ui.components.InfoIconButton
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyCheckRowData

@Composable
internal fun <T> PropertyCheckRow(
    data: PropertyCheckRowData<T>,
    modifier: Modifier = Modifier,
    onInfoButtonClick: (() -> Unit)? = null,
) {
    PropertyCheckRow(
        data = data,
        modifier = modifier,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
            )
        },
        onInfoButtonClick = onInfoButtonClick,
    )
}

@Composable
internal fun <T> SubPropertyCheckRow(
    data: PropertyCheckRowData<T>,
    modifier: Modifier = Modifier,
    onInfoButtonClick: (() -> Unit)? = null,
) {
    PropertyCheckRow(
        data = data,
        modifier = modifier.padding(start = 16.dp),
        fontSize = 14.sp,
        makeText = ::bulletPointText,
        onInfoButtonClick = onInfoButtonClick,
    )
}

@Composable
private fun <T> PropertyCheckRow(
    data: PropertyCheckRowData<T>,
    modifier: Modifier = Modifier,
    makeText: (String) -> String = { it },
    fontSize: TextUnit = TextUnit.Unspecified,
    leadingIcon: (@Composable () -> Unit)? = null,
    onInfoButtonClick: (() -> Unit)? = null,
) {
    val label = stringResource(id = data.labelRes)
    val checkBoxCD = stringResource(id = R.string.set_unset, label)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth(),
    ) {

        leadingIcon?.invoke()
        AppFontText(
            text = makeText(label),
            fontSize = fontSize,
            modifier = Modifier.weight(1.0f, true),
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
        onInfoButtonClick?.let {
            InfoIconButton(
                onClick = it,
                contentDescription = stringResource(id = R.string.info_icon_cd, label),
            )
        }
    }
}
