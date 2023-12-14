package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.PropertyCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.SubPropertyCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyInfoDialogData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun RefreshingParametersSelection(
    parameterIsChecked: (WidgetRefreshingParameter) -> Boolean,
    onParameterCheckedChanged: (WidgetRefreshingParameter, Boolean) -> Unit,
    showInfoDialog: (PropertyInfoDialogData) -> Unit,
    scrollToContentColumnBottom: suspend () -> Unit,
    modifier: Modifier = Modifier,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val parameterViewData = remember {
        WidgetRefreshingParameter.entries
            .map { parameter ->
                PropertyCheckRowData(
                    property = parameter,
                    labelRes = parameter.labelRes,
                    isChecked = { parameterIsChecked(parameter) },
                    onCheckedChange = { onParameterCheckedChanged(parameter, it) }
                )
            }
    }

    // TODO: try to simplify
    Column(modifier = modifier) {
        PropertyCheckRow(
            data = parameterViewData[0],
            onInfoButtonClick = {
                showInfoDialog(
                    PropertyInfoDialogData(
                        labelRes = parameterViewData[0].labelRes,
                        descriptionRes = R.string.refresh_periodically_info,
                    ),
                )
            },
        )
        AnimatedVisibility(
            visible = parameterViewData[0].isChecked(),
            enter = fadeIn() + expandVertically(initialHeight = { 0.also { scope.launch { scrollToContentColumnBottom() } } }),
        ) {
            SubPropertyCheckRow(
                data = parameterViewData[1],
            )
        }
        PropertyCheckRow(
            data = parameterViewData[2],
        )
    }
}
