package com.w2sv.wifiwidget.ui.screen.widgetconfig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.isLandscapeModeActive
import com.w2sv.core.common.R
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarHost
import com.w2sv.wifiwidget.ui.designsystem.BackButtonHeaderWithBottomDivider
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.screen.widgetconfig.list.UpdateWidgetConfig
import com.w2sv.wifiwidget.ui.screen.widgetconfig.list.WidgetConfigList
import com.w2sv.wifiwidget.ui.util.PreviewOf
import com.w2sv.wifiwidget.ui.util.ScreenPreviews
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun WidgetConfigScreen(
    config: WidgetConfig,
    updateConfig: UpdateWidgetConfig,
    configEditState: ConfigEditState,
    showDialog: (WidgetConfigDialog) -> Unit,
    onBackButtonClick: () -> Unit,
    state: LazyListState = rememberLazyListState()
) {
    Scaffold(
        snackbarHost = { AppSnackbarHost() },
        floatingActionButton = {
            EditingFabButtonRow(
                configEditState = configEditState,
                modifier = Modifier
                    .padding(
                        top = 8.dp, // Snackbar padding
                        end = if (isLandscapeModeActive) 38.dp else 0.dp
                    )
                    .height(70.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding() + 16.dp)
        ) {
            BackButtonHeaderWithBottomDivider(
                title = stringResource(id = R.string.widget_configuration),
                onBackButtonClick = onBackButtonClick
            )

            WidgetConfigList(
                state = state,
                config = config,
                updateConfig = updateConfig,
                showDialog = showDialog,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = 800.dp)
            )
        }
    }
}

@ScreenPreviews
@Composable
private fun Prev() {
    PreviewOf {
        WidgetConfigScreen(
            config = WidgetConfig.default,
            updateConfig = {},
            configEditState = ConfigEditState(
                { true },
                {},
                {},
                emptyFlow()
            ),
            showDialog = {},
            onBackButtonClick = {},
            state = LazyListState(firstVisibleItemIndex = 0)
        )
    }
}
