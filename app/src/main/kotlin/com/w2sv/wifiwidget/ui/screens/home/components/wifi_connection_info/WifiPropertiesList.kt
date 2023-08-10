package com.w2sv.wifiwidget.ui.screens.home.components.wifi_connection_info

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.utils.enumerationTag
import com.w2sv.data.model.WifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.SnackbarKind

@Stable
data class WifiPropertyViewData(val property: WifiProperty, val value: WifiProperty.Value)

@Composable
fun WifiPropertiesList(
    propertiesViewData: List<WifiPropertyViewData>,
    showSnackbar: (SnackbarVisuals) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                JostText(
                    text = stringResource(id = R.string.properties),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                JostText(
                    text = stringResource(R.string.click_to_copy_to_clipboard),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
        items(propertiesViewData) { viewData ->
            val propertyName = stringResource(id = viewData.property.viewData.labelRes)

            when (val value = viewData.value) {
                is WifiProperty.Value.Singular -> {
                    WifiPropertyRow(
                        propertyName = propertyName,
                        value = value.value,
                        showSnackbar = showSnackbar
                    )
                }

                is WifiProperty.Value.IPAddresses -> {
                    if (value.addresses.size > 1) {
                        value.addresses.forEachIndexed { i, address ->
                            WifiPropertyRow(
                                propertyName = "$propertyName ${enumerationTag(i)}",
                                value = address.textualRepresentation,
                                showSnackbar = showSnackbar
                            )
                        }
                    } else if (value.addresses.size == 1) {
                        WifiPropertyRow(
                            propertyName = propertyName,
                            value = value.addresses.first().textualRepresentation,
                            showSnackbar = showSnackbar
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WifiPropertyRow(
    propertyName: String,
    value: String,
    showSnackbar: (SnackbarVisuals) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .clickable {
                clipboardManager.setText(AnnotatedString(value))
                showSnackbar(
                    AppSnackbarVisuals(
                        message = context.getString(R.string.copied_to_clipboard, propertyName),
                        kind = SnackbarKind.Success
                    )
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        JostText(
            text = propertyName,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        JostText(
            text = value,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}