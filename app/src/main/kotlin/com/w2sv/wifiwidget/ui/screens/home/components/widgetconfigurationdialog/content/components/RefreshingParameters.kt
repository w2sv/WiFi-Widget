package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.content.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.w2sv.data.model.widget.WidgetRefreshingParameter
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.content.InfoDialogButtonData
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.content.InfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.content.PropertyCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.content.PropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfigurationdialog.content.SubPropertyCheckRow
import kotlinx.coroutines.launch

@Composable
internal fun RefreshingParametersSelection(
    widgetRefreshingMap: MutableMap<WidgetRefreshingParameter, Boolean>,
    showInfoDialog: (InfoDialogData) -> Unit,
    scrollToContentColumnBottom: suspend () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val parameterViewData = remember {
        listOf(
            PropertyCheckRowData(
                type = WidgetRefreshingParameter.RefreshPeriodically,
                labelRes = R.string.refresh_periodically,
                isCheckedMap = widgetRefreshingMap
            ),
            PropertyCheckRowData(
                type = WidgetRefreshingParameter.RefreshOnLowBattery,
                labelRes = R.string.refresh_on_low_battery,
                isCheckedMap = widgetRefreshingMap
            ),
            PropertyCheckRowData(
                type = WidgetRefreshingParameter.DisplayLastRefreshDateTime,
                labelRes = R.string.display_last_refresh_time,
                isCheckedMap = widgetRefreshingMap
            )
        )
    }

    Column(modifier = modifier) {
        PropertyCheckRow(
            data = parameterViewData[0],
            infoDialogButtonData = InfoDialogButtonData {
                showInfoDialog(
                    InfoDialogData(
                        labelRes = R.string.refresh_periodically,
                        descriptionRes = R.string.refresh_periodically_info,
                    )
                )
            }
        )
        AnimatedVisibility(
            visible = parameterViewData[0].isChecked(),
            enter = fadeIn() + expandVertically(initialHeight = { 0.also { scope.launch { scrollToContentColumnBottom() } } })
        ) {
            SubPropertyCheckRow(
                data = parameterViewData[1]
            )
        }
        PropertyCheckRow(
            data = parameterViewData[2]
        )
    }
}