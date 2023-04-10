package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.configcolumn

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.WidgetRefreshingParameter
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.WidgetConfigurationViewModel
import com.w2sv.wifiwidget.ui.shared.JostText
import kotlinx.coroutines.launch

@Composable
internal fun RefreshingSection(
    modifier: Modifier = Modifier,
    viewModel: WidgetConfigurationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    scrollToContentColumnBottom: suspend () -> Unit
) {
    val scope = rememberCoroutineScope()

    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        RefreshingParameterRow(
            label = R.string.refresh_periodically,
            parameter = WidgetRefreshingParameter.RefreshPeriodically
        )
        AnimatedVisibility(
            visible = viewModel.widgetRefreshingParametersState.getValue(WidgetRefreshingParameter.RefreshPeriodically.name),
            enter = fadeIn() + expandVertically(initialHeight = { scope.launch { scrollToContentColumnBottom() }; 0 })
        ) {
            RefreshingParameterRow(
                label = R.string.refresh_on_low_battery,
                parameter = WidgetRefreshingParameter.RefreshOnBatteryLow,
                modifier = Modifier.padding(start = 12.dp),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun RefreshingParameterRow(
    @StringRes label: Int,
    parameter: WidgetRefreshingParameter,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    viewModel: WidgetConfigurationViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        JostText(text = stringResource(id = label), fontSize = fontSize)
        Checkbox(
            checked = viewModel.widgetRefreshingParametersState.getValue(parameter.name),
            onCheckedChange = {
                viewModel.widgetRefreshingParametersState[parameter.name] = it
            }
        )
    }
}