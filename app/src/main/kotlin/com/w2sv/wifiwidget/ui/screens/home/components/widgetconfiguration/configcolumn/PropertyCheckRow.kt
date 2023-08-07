package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.bulletPointText
import com.w2sv.wifiwidget.ui.theme.disabledColor

@Stable
open class PropertyCheckRowData<T>(
    val type: T,
    @StringRes val labelRes: Int,
    val isChecked: () -> Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    val allowCheckChange: (Boolean) -> Boolean = { true }
) {
    constructor(
        type: T,
        @StringRes labelRes: Int,
        isCheckedMap: MutableMap<T, Boolean>,
        allowCheckChange: (Boolean) -> Boolean = { true }
    ) : this(
        type = type,
        labelRes = labelRes,
        isChecked = { isCheckedMap.getValue(type) },
        onCheckedChange = { isCheckedMap[type] = it },
        allowCheckChange = allowCheckChange
    )
}

@Composable
internal fun <T> PropertyCheckRow(
    data: PropertyCheckRowData<T>,
    modifier: Modifier = Modifier,
    trailingIconButton: (@Composable () -> Unit)? = null
) {
    PropertyCheckRow(
        data = data,
        modifier = modifier,
        leadingIcon = { color: Color ->
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = color
            )
        },
        trailingIconButton = trailingIconButton
    )
}

@Composable
internal fun <T> SubPropertyCheckRow(
    data: PropertyCheckRowData<T>,
    modifier: Modifier = Modifier,
    trailingIconButton: (@Composable () -> Unit)? = null
) {
    PropertyCheckRow(
        data = data,
        modifier = modifier.padding(start = 22.dp),
        fontSize = 14.sp,
        makeText = ::bulletPointText,
        trailingIconButton = trailingIconButton
    )
}

@Composable
private fun <T> PropertyCheckRow(
    data: PropertyCheckRowData<T>,
    modifier: Modifier = Modifier,
    makeText: (String) -> String = { it },
    fontSize: TextUnit = TextUnit.Unspecified,
    leadingIcon: (@Composable (Color) -> Unit)? = null,
    trailingIconButton: (@Composable () -> Unit)? = null
) {
    val checkBoxCD = stringResource(id = R.string.set_unset).format(data.labelRes)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
    ) {
        val color = if (data.isChecked()) LocalContentColor.current else disabledColor()

        leadingIcon?.invoke(color)
        JostText(
            text = makeText(stringResource(id = data.labelRes)),
            fontSize = fontSize,
            modifier = Modifier.weight(1.0f, true),
            color = color
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
            }
        )
        trailingIconButton?.invoke()
    }
}