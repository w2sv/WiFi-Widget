package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.common.utils.minutes
import com.w2sv.core.common.R
import com.w2sv.domain.model.widget.WidgetRefreshing
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.designsystem.configlist.CheckableList
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigItem
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
            iconRes = R.drawable.ic_refresh_24,
            stringRes = R.string.refreshing
        )
    ) {
        CheckableList(
            elements = persistentListOf(
                refreshingConfig(
                    refreshing = refreshing,
                    updateRefreshing = updateRefreshing,
                    showDialog = showDialog
                )
            ),
            modifier = modifier
        )
    }
}

private fun refreshingConfig(
    refreshing: WidgetRefreshing,
    updateRefreshing: (WidgetRefreshing.() -> WidgetRefreshing) -> Unit,
    showDialog: (WidgetConfigDialog) -> Unit
): ConfigItem.Checkable =
    ConfigItem.Checkable(
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
        contentBeneath = subSettings(
            refreshing = refreshing,
            updateRefreshing = updateRefreshing,
            showDialog = showDialog
        )
    )

private fun subSettings(
    refreshing: WidgetRefreshing,
    updateRefreshing: (WidgetRefreshing.() -> WidgetRefreshing) -> Unit,
    showDialog: (WidgetConfigDialog) -> Unit
) =
    ConfigItem.SubSettings(
        elements = persistentListOf(
            ConfigItem.WithCustomTrailing(property = WidgetRefreshingParameter.Interval) {
                ConfigureIntervalRow(
                    interval = refreshing.interval,
                    showConfigurationDialog = { showDialog(WidgetConfigDialog.RefreshIntervalPicker(refreshing.interval)) }
                )
            },
            ConfigItem.Checkable(
                property = WidgetRefreshingParameter.RefreshOnLowBattery,
                isChecked = { refreshing.refreshOnLowBattery },
                onCheckedChange = { updateRefreshing { copy(refreshOnLowBattery = it) } }
            )
        ),
        allowCollapsing = false
    )

@Composable
private fun RowScope.ConfigureIntervalRow(interval: Duration, showConfigurationDialog: () -> Unit) {
    Text(text = remember(interval) { interval.toReadableString() })
    IconButton(
        onClick = showConfigurationDialog,
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_edit_24),
            contentDescription = stringResource(R.string.open_the_refresh_interval_configuration_dialog),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

private fun Duration.toReadableString(): String =
    when {
        inWholeHours == 0L -> "${minutes}m"
        minutes == 0 -> "${inWholeHours}h"
        else -> "${inWholeHours}h ${minutes}m"
    }
