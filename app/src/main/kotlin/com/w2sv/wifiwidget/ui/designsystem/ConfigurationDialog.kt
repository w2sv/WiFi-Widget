package com.w2sv.wifiwidget.ui.designsystem

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.w2sv.core.common.R

private val elevatedCardDialogElevation = 16.dp

@Composable
fun ConfigurationDialog(
    onDismissRequest: () -> Unit,
    onApplyButtonPress: () -> Unit,
    modifier: Modifier = Modifier,
    columnModifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null,
    title: String? = null,
    applyButtonEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        ElevatedCard(
            modifier = modifier,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevatedCardDialogElevation)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = columnModifier.padding(vertical = 22.dp, horizontal = 14.dp)
            ) {
                iconRes?.let {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                content()
                CancelApplyButtonRow(
                    onCancel = onDismissRequest,
                    onApply = onApplyButtonPress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    applyButtonEnabled = applyButtonEnabled
                )
            }
        }
    }
}

@Composable
private fun CancelApplyButtonRow(
    onCancel: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
    applyButtonEnabled: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DialogButton(onClick = onCancel) {
            Text(text = stringResource(R.string.cancel))
        }
        Spacer(modifier = Modifier.width(16.dp))
        DialogButton(onClick = onApply, enabled = applyButtonEnabled) {
            Text(text = stringResource(R.string.apply))
        }
    }
}
