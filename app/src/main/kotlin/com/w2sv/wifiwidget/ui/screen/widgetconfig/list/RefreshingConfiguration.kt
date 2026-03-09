package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.common.utils.minutes
import com.w2sv.domain.model.widget.WidgetRefreshing
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.designsystem.SubPropertyKeyboardArrowRightIcon
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.screen.widgetconfig.model.WidgetRefreshingParameter
import kotlin.time.Duration
import kotlinx.collections.immutable.persistentListOf

@Composable
fun RefreshingConfigCard(
    refreshing: WidgetRefreshing,
    updateRefreshing: (WidgetRefreshing.() -> WidgetRefreshing) -> Unit,
    showDialog: (WidgetConfigDialog) -> Unit,
    modifier: Modifier = Modifier
) {
    WidgetConfigSectionCard(
        header = IconHeader(
            iconRes = com.w2sv.core.common.R.drawable.ic_refresh_24,
            stringRes = R.string.refreshing
        )
    ) {
        CheckRowColumn(
            elements = persistentListOf(
                refreshingCheckRow(
                    refreshing = refreshing,
                    updateRefreshing = updateRefreshing,
                    showDialog = showDialog
                )
            ),
            modifier = modifier
        )
    }
}

private fun refreshingCheckRow(
    refreshing: WidgetRefreshing,
    updateRefreshing: (WidgetRefreshing.() -> WidgetRefreshing) -> Unit,
    showDialog: (WidgetConfigDialog) -> Unit
): ConfigListElement.CheckRow =
    ConfigListElement.CheckRow(
        property = WidgetRefreshingParameter.RefreshPeriodically,
        isChecked = { refreshing.refreshPeriodically },
        onCheckedChange = { updateRefreshing { copy(refreshPeriodically = it) } },
        showInfoDialog = {
            showDialog(
                WidgetConfigDialog.Info(
                    titleRes = WidgetRefreshingParameter.RefreshPeriodically.labelRes,
                    descriptionRes = R.string.refresh_periodically_info
                )
            )
        },
        subPropertyColumnElements = persistentListOf(
            ConfigListElement.Custom {
                RefreshIntervalConfigurationRow(
                    interval = refreshing.interval,
                    showConfigurationDialog = { showDialog(WidgetConfigDialog.RefreshIntervalPicker(refreshing.interval)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            },
            ConfigListElement.CheckRow(
                property = WidgetRefreshingParameter.RefreshOnLowBattery,
                isChecked = { refreshing.refreshOnLowBattery },
                onCheckedChange = { updateRefreshing { copy(refreshOnLowBattery = it) } }
            )
        )
    )

@Composable
private fun RefreshIntervalConfigurationRow(
    interval: Duration,
    showConfigurationDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    PropertyConfigurationRow(
        R.string.interval,
        modifier = modifier,
        fontSize = SubPropertyColumnDefaults.fontSize,
        leadingIcon = { SubPropertyKeyboardArrowRightIcon() }
    ) {
        Text(text = remember(interval) { interval.toReadableString() })
        FilledTonalIconButton(
            onClick = showConfigurationDialog,
            modifier = Modifier
                .padding(start = 8.dp, end = 4.dp)
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

private fun Duration.toReadableString(): String =
    when {
        inWholeHours == 0L -> "${minutes}m"
        minutes == 0 -> "${inWholeHours}h"
        else -> "${inWholeHours}h ${minutes}m"
    }
