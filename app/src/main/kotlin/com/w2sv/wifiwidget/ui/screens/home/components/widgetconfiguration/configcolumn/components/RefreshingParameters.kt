package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.data.model.WidgetRefreshingParameter
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.ParameterCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.ParameterCheckRowData
import kotlinx.coroutines.launch

@Composable
internal fun RefreshingParametersSelection(
    widgetRefreshingMap: MutableMap<WidgetRefreshingParameter, Boolean>,
    scrollToContentColumnBottom: suspend () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val parameterViewData = remember {
        listOf(
            ParameterCheckRowData(
                type = WidgetRefreshingParameter.RefreshPeriodically,
                labelRes = R.string.refresh_periodically
            ),
            ParameterCheckRowData(
                type = WidgetRefreshingParameter.RefreshOnLowBattery,
                labelRes = R.string.refresh_on_low_battery
            ),
            ParameterCheckRowData(
                type = WidgetRefreshingParameter.DisplayLastRefreshDateTime,
                labelRes = R.string.display_last_refresh_time
            )
        )
    }

    Column(modifier = modifier) {
        ParameterCheckRow(
            data = parameterViewData[0],
            typeToIsChecked = widgetRefreshingMap
        )
        AnimatedVisibility(
            visible = widgetRefreshingMap.getValue(
                WidgetRefreshingParameter.RefreshPeriodically
            ),
            enter = fadeIn() + expandVertically(initialHeight = { 0.also { scope.launch { scrollToContentColumnBottom() } } })
        ) {
            ParameterCheckRow(
                data = parameterViewData[1],
                typeToIsChecked = widgetRefreshingMap,
                modifier = Modifier.padding(start = 12.dp),
                fontSize = 14.sp
            )
        }
        ParameterCheckRow(
            data = parameterViewData[2],
            typeToIsChecked = widgetRefreshingMap
        )
    }
}