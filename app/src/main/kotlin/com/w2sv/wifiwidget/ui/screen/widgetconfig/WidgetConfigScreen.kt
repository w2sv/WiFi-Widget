package com.w2sv.wifiwidget.ui.screen.widgetconfig

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.composed.ui.platform.isLandscapeModeActive
import com.w2sv.core.common.R
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarHost
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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text(stringResource(R.string.widget_configuration)) },
                collapsedHeight = 56.dp,
                navigationIcon = {
                    IconButton(onClick = onBackButtonClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
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
        },
        contentWindowInsets = WindowInsets()
    ) { paddingValues ->
        WidgetConfigList(
            state = state,
            config = config,
            updateConfig = updateConfig,
            showDialog = showDialog,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
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
