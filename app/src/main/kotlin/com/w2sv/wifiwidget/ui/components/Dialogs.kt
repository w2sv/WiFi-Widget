package com.w2sv.wifiwidget.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.utils.conditional

@Composable
fun CustomDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    headerProperties: DialogHeaderProperties? = null,
    scrollState: ScrollState? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        ElevatedCard(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.elevatedCardElevation(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(vertical = 24.dp, horizontal = 28.dp)
                    .conditional(scrollState != null, { verticalScroll(scrollState!!) })
            ) {
                headerProperties?.let {
                    DialogHeader(properties = it)
                }
                content()
            }
        }
    }
}

@Stable
data class DialogHeaderProperties(
    val title: String,
    val icon: (@Composable () -> Unit)? = null
)

@Composable
fun DialogHeader(properties: DialogHeaderProperties) {
    properties.icon?.let {
        it.invoke()
        Spacer(modifier = Modifier.height(12.dp))
    }
    JostText(
        text = properties.title,
        style = MaterialTheme.typography.headlineSmall
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun DialogButtonRow(
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
            JostText(text = stringResource(R.string.cancel))
        }
        Spacer(modifier = Modifier.width(16.dp))
        DialogButton(onClick = onApply, enabled = applyButtonEnabled) {
            JostText(text = stringResource(R.string.apply))
        }
    }
}