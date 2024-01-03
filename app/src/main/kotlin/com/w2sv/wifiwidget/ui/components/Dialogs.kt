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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.utils.conditional

@Composable
fun AppDialog(
    onDismissRequest: () -> Unit,
    header: DialogHeader,
    modifier: Modifier = Modifier,
    scrollState: ScrollState? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        ElevatedCard(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.elevatedCardElevation(16.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(vertical = 24.dp, horizontal = 28.dp)
                    .conditional(scrollState != null, { verticalScroll(scrollState!!) }),
            ) {
                DialogHeader(properties = header)
                content()
            }
        }
    }
}

@Immutable
data class DialogHeader(
    val title: String,
    val icon: (@Composable () -> Unit)? = null,
)

@Composable
private fun DialogHeader(properties: DialogHeader, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        properties.icon?.let {
            it.invoke()
            Spacer(modifier = Modifier.height(12.dp))
        }
        Text(
            text = properties.title,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun DialogBottomButtonRow(
    onCancel: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
    applyButtonEnabled: Boolean = true,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
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
