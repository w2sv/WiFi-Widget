package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration_column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.common.utils.minutes
import com.w2sv.wifiwidget.R
import kotlin.time.Duration

@Composable
fun RefreshIntervalConfigurationRow(
    interval: Duration,
    showConfigurationDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubPropertyKeyboardArrowRightIcon()
        Text(text = stringResource(R.string.interval))
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = remember(interval) {
                interval.run {
                    when {
                        inWholeHours == 0L -> "${minutes}m"
                        minutes == 0 -> "${inWholeHours}h"
                        else -> "${inWholeHours}h ${minutes}m"
                    }
                }
            }
        )
        IconButton(
            onClick = showConfigurationDialog,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(38.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_edit_24),
                contentDescription = stringResource(R.string.open_the_refresh_interval_configuration_dialog),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}