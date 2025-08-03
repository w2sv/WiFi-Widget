package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.common.utils.minutes
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.IconHeaderProperties
import com.w2sv.wifiwidget.ui.designsystem.SubPropertyKeyboardArrowRightIcon
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.InfoDialogData
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.ReversibleWidgetConfiguration
import kotlin.time.Duration
import kotlinx.collections.immutable.persistentListOf

fun refreshingConfigurationCard(
    widgetConfiguration: ReversibleWidgetConfiguration,
    showInfoDialog: (InfoDialogData) -> Unit,
    showRefreshIntervalConfigurationDialog: () -> Unit,
    contentModifier: Modifier = Modifier
): WidgetConfigurationCard =
    WidgetConfigurationCard(
        iconHeaderProperties = IconHeaderProperties(
            iconRes = com.w2sv.core.common.R.drawable.ic_refresh_24,
            stringRes = R.string.refreshing
        )
    ) {
        CheckRowColumn(
            elements = remember {
                persistentListOf(
                    ConfigurationColumnElement.CheckRow.fromIsCheckedMap(
                        property = WidgetRefreshingParameter.RefreshPeriodically,
                        isCheckedMap = widgetConfiguration.refreshingParametersMap,
                        showInfoDialog = {
                            showInfoDialog(
                                InfoDialogData(
                                    titleRes = WidgetRefreshingParameter.RefreshPeriodically.labelRes,
                                    descriptionRes = R.string.refresh_periodically_info
                                )
                            )
                        },
                        subPropertyColumnElements = persistentListOf(
                            ConfigurationColumnElement.Custom {
                                val refreshInterval by widgetConfiguration.refreshInterval.collectAsStateWithLifecycle()
                                RefreshIntervalConfigurationRow(
                                    interval = refreshInterval,
                                    showConfigurationDialog = showRefreshIntervalConfigurationDialog,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                )
                            },
                            ConfigurationColumnElement.CheckRow.fromIsCheckedMap(
                                property = WidgetRefreshingParameter.RefreshOnLowBattery,
                                isCheckedMap = widgetConfiguration.refreshingParametersMap
                            )
                        )
                    )
                )
            },
            modifier = contentModifier
        )
    }

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
