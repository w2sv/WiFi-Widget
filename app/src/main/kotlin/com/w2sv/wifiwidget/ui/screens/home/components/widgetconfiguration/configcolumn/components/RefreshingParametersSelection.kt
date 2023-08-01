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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.data.model.WidgetRefreshingParameter
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.bulletPointText
import kotlinx.coroutines.launch

@Stable
private data class RefreshingParameterViewData(
    val label: String,
    val parameter: WidgetRefreshingParameter
)

@Composable
internal fun RefreshingParametersSelection(
    widgetRefreshingMap: MutableMap<WidgetRefreshingParameter, Boolean>,
    scrollToContentColumnBottom: suspend () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val parameterViewData by remember {
        derivedStateOf {
            listOf(
                RefreshingParameterViewData(
                    label = "Refresh periodically",
                    parameter = WidgetRefreshingParameter.RefreshPeriodically
                ),
                RefreshingParameterViewData(
                    label = "Refresh on low battery",
                    parameter = WidgetRefreshingParameter.RefreshOnLowBattery
                ),
                RefreshingParameterViewData(
                    label = "Display last refresh date time",
                    parameter = WidgetRefreshingParameter.DisplayLastRefreshDateTime
                )
            )
        }
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        RefreshingParameterView(
            data = parameterViewData[0],
            widgetRefreshingMap = widgetRefreshingMap
        )
        AnimatedVisibility(
            visible = widgetRefreshingMap.getValue(
                WidgetRefreshingParameter.RefreshPeriodically
            ),
            enter = fadeIn() + expandVertically(initialHeight = { 0.also { scope.launch { scrollToContentColumnBottom() } } })
        ) {
            RefreshingParameterView(
                data = parameterViewData[1],
                widgetRefreshingMap = widgetRefreshingMap,
                modifier = Modifier.padding(start = 12.dp),
                fontSize = 14.sp
            )
        }
        RefreshingParameterView(
            data = parameterViewData[2],
            widgetRefreshingMap = widgetRefreshingMap
        )
    }
}

@Composable
private fun RefreshingParameterView(
    data: RefreshingParameterViewData,
    widgetRefreshingMap: MutableMap<WidgetRefreshingParameter, Boolean>,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified
) {
    val checkBoxCD = stringResource(id = R.string.set_unset).format(data.label)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        JostText(text = bulletPointText(data.label), fontSize = fontSize)
        Checkbox(
            checked = widgetRefreshingMap.getValue(data.parameter),
            onCheckedChange = {
                widgetRefreshingMap[data.parameter] = it
            },
            modifier = Modifier.semantics {
                contentDescription = checkBoxCD
            }
        )
    }
}