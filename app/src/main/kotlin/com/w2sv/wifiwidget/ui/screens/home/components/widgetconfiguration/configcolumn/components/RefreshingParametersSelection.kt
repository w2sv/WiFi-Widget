package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components

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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.common.enums.WidgetRefreshingParameter
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.WidgetConfigurationViewModel
import kotlinx.coroutines.launch

@Composable
internal fun RefreshingParametersSelection(
    scrollToContentColumnBottom: suspend () -> Unit,
    modifier: Modifier = Modifier,
    widgetConfigurationVM: WidgetConfigurationViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()

    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        RefreshingParameterRow(
            parameter = WidgetRefreshingParameter.RefreshPeriodically
        )
        AnimatedVisibility(
            visible = widgetConfigurationVM.nonAppliedWidgetRefreshingParameterFlags.getValue(
                WidgetRefreshingParameter.RefreshPeriodically
            ),
            enter = fadeIn() + expandVertically(initialHeight = { scope.launch { scrollToContentColumnBottom() }; 0 })
        ) {
            RefreshingParameterRow(
                parameter = WidgetRefreshingParameter.RefreshOnBatteryLow,
                modifier = Modifier.padding(start = 12.dp),
                fontSize = 14.sp
            )
        }
        RefreshingParameterRow(parameter = WidgetRefreshingParameter.ShowDateTime)
    }
}

@Composable
private fun RefreshingParameterRow(
    parameter: WidgetRefreshingParameter,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    widgetConfigurationVM: WidgetConfigurationViewModel = viewModel()
) {
    val label = stringResource(id = parameter.labelRes)
    val checkBoxCD = stringResource(id = R.string.set_unset).format(label)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        JostText(text = label, fontSize = fontSize)
        Checkbox(
            checked = widgetConfigurationVM.nonAppliedWidgetRefreshingParameterFlags.getValue(
                parameter
            ),
            onCheckedChange = {
                widgetConfigurationVM.nonAppliedWidgetRefreshingParameterFlags[parameter] = it
            },
            modifier = Modifier.semantics {
                contentDescription = checkBoxCD
            }
        )
    }
}