package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration_column

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.composed.extensions.thenIf
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.InfoIcon
import com.w2sv.wifiwidget.ui.designsystem.KeyboardArrowRightIcon
import com.w2sv.wifiwidget.ui.designsystem.biggerIconSize
import com.w2sv.wifiwidget.ui.designsystem.nestedContentBackground
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.SubPropertyKeyboardArrowRightIcon
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.InfoDialogData
import kotlinx.collections.immutable.ImmutableList

// For alignment of primary check row click elements and sub property click elements
private val primaryCheckRowModifier = Modifier.padding(end = 16.dp)

@Composable
fun PropertyCheckRowColumn(
    dataList: ImmutableList<PropertyConfigurationView.CheckRow<*>>,
    modifier: Modifier = Modifier,
    showInfoDialog: ((InfoDialogData) -> Unit)? = null
) {
    Column(modifier = modifier) {
        dataList
            .forEach { data ->
                when (data.hasSubProperties) {
                    false -> {
                        PropertyCheckRow(data = data, showInfoDialog = showInfoDialog)
                    }

                    true -> {
                        PropertyCheckRowWithSubProperties(
                            data = data,
                            showInfoDialog = showInfoDialog
                        )
                    }
                }
            }
    }
}

@Composable
private fun PropertyCheckRow(
    data: PropertyConfigurationView.CheckRow<*>,
    showInfoDialog: ((InfoDialogData) -> Unit)?
) {
    PropertyCheckRow(
        data = data,
        showInfoDialog = showInfoDialog,
        leadingIcon = {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                KeyboardArrowRightIcon(tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        modifier = primaryCheckRowModifier
    )
}

@Composable
private fun PropertyCheckRowWithSubProperties(
    data: PropertyConfigurationView.CheckRow<*>,
    showInfoDialog: ((InfoDialogData) -> Unit)?
) {
    var expandSubProperties by rememberSaveable {
        mutableStateOf(false)
    }
    // Collapse subProperties on unchecking
    LaunchedEffect(data, data.isChecked()) {
        if (!data.isChecked()) {
            expandSubProperties = false
        }
    }

    Column {
        PropertyCheckRow(
            data = data,
            showInfoDialog = showInfoDialog,
            leadingIcon = {
                IconButton(
                    onClick = remember {
                        {
                            expandSubProperties = !expandSubProperties
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    enabled = data.isChecked()
                ) {
                    Icon(
                        imageVector = if (expandSubProperties) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            },
            modifier = primaryCheckRowModifier
        )

        AnimatedVisibility(visible = expandSubProperties) {
            SubPropertyCheckRowColumn(
                configurationElements = data.subPropertyCheckRowDataList,
                modifier = data.subPropertyColumnModifier
                    .padding(start = 24.dp)  // Make background start at the indentation of PropertyCheckRow label
                    .nestedContentBackground()
                    .padding(start = subPropertyColumnPadding)
            )
        }
    }
}

@Composable
private fun SubPropertyCheckRowColumn(
    configurationElements: ImmutableList<PropertyConfigurationView>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        configurationElements.forEach { element ->
            when (element) {
                is PropertyConfigurationView.CheckRow<*> -> {
                    if ((element.property as? WidgetWifiProperty.IP.SubProperty)?.kind is WidgetWifiProperty.IP.V4AndV6.AddressTypeEnablement.V4Enabled) {
                        Text(
                            text = stringResource(R.string.versions),
                            modifier = Modifier.padding(top = subPropertyColumnPadding),
                            fontSize = subPropertyCheckRowColumnFontSize,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    PropertyCheckRow(
                        data = element,
                        modifier = Modifier.thenIf(
                            condition = (element.property as? WidgetWifiProperty.IP.SubProperty)?.isAddressTypeEnablementProperty == true,
                            onTrue = { padding(start = addressVersionEnablementStartPadding) }
                        ),
                        fontSize = subPropertyCheckRowColumnFontSize,
                        leadingIcon = {
                            SubPropertyKeyboardArrowRightIcon()
                        }
                    )
                }

                is PropertyConfigurationView.Custom -> {
                    element.content()
                }
            }
        }
    }
}

private val subPropertyCheckRowColumnFontSize = 14.sp
private val subPropertyColumnPadding = 12.dp
private val addressVersionEnablementStartPadding = 16.dp

@Composable
private fun PropertyCheckRow(
    data: PropertyConfigurationView.CheckRow<*>,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    textColor: Color = Color.Unspecified,
    leadingIcon: (@Composable () -> Unit)? = null,
    showInfoDialog: ((InfoDialogData) -> Unit)? = null,
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
            text = label,
            fontSize = fontSize,
            modifier = Modifier.weight(1.0f, true),
            color = textColor
        )
        if (showInfoDialog != null && data.infoDialogData != null) {
            InfoIconButton(
                onClick = { showInfoDialog(data.infoDialogData) },
                contentDescription = stringResource(id = R.string.info_icon_cd, label),
            )
        }
        Checkbox(
            checked = data.isChecked(),
            onCheckedChange = {
                data.onCheckedChange(it)
            },
            modifier = Modifier.semantics {
                contentDescription = checkBoxCD
            },
        )
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
            modifier = Modifier.size(biggerIconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
