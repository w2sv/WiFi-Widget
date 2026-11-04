package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.w2sv.common.time.minutes
import com.w2sv.core.resources.R
import com.w2sv.domain.widget.WidgetRefreshing
import com.w2sv.wifiwidget.ui.designsystem.ElevatedCardWithHeader
import com.w2sv.wifiwidget.ui.designsystem.NestedSettingsTokens
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigItem
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigItemLayout
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigListTokens
import com.w2sv.wifiwidget.ui.designsystem.nestedContentBackground
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.screen.widgetconfig.model.WidgetRefreshingParameter
import kotlin.time.Duration

@Composable
fun RefreshingConfigCard(
    refreshing: WidgetRefreshing,
    updateRefreshing: (WidgetRefreshing.() -> WidgetRefreshing) -> Unit,
    showDialog: (WidgetConfigDialog) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCardWithHeader(
        iconRes = R.drawable.ic_refresh_24,
        stringRes = R.string.refreshing,
        innerPadding = WidgetConfigListDimens.sectionCardInnerPadding,
        modifier = modifier
    ) {
        ConfigItemLayout(
            item = refreshingConfig(refreshing, updateRefreshing, showDialog),
            modifier = Modifier.padding(start = ConfigListTokens.startPaddingIfNoToggleButton, end = ConfigListTokens.checkRowEndPadding),
            allowCollapsing = false,
            expandedContent = { RefreshIntervalSubSettings(refreshing, showDialog) }
        )
    }
}

private fun refreshingConfig(
    refreshing: WidgetRefreshing,
    updateRefreshing: (WidgetRefreshing.() -> WidgetRefreshing) -> Unit,
    showDialog: (WidgetConfigDialog) -> Unit
): ConfigItem =
    ConfigItem(
        property = WidgetRefreshingParameter.RefreshPeriodically,
        isChecked = { refreshing.refreshPeriodically },
        trySetChecked = {
            updateRefreshing { copy(refreshPeriodically = it) }
            true
        },
        showInfoDialog = {
            showDialog(
                WidgetConfigDialog.Info(
                    titleRes = WidgetRefreshingParameter.RefreshPeriodically.labelRes,
                    descriptionRes = R.string.refresh_periodically_info
                )
            )
        }
    )

@Composable
private fun RefreshIntervalSubSettings(refreshing: WidgetRefreshing, showDialog: (WidgetConfigDialog) -> Unit) {
    Column(
        modifier = Modifier
            .padding(bottom = NestedSettingsTokens.bottomMargin)
            .nestedContentBackground()
            .padding(start = NestedSettingsTokens.startPadding)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(WidgetRefreshingParameter.Interval.labelRes),
                fontSize = NestedSettingsTokens.fontSize,
                modifier = Modifier.weight(1f)
            )
            ConfigureIntervalRow(
                interval = refreshing.interval,
                showConfigurationDialog = { showDialog(WidgetConfigDialog.RefreshIntervalPicker(refreshing.interval)) }
            )
        }
    }
}

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
