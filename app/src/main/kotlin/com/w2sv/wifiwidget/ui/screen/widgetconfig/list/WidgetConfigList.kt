package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.isPortraitModeActive
import com.w2sv.domain.model.widget.WifiWidgetConfig
import com.w2sv.kotlinutils.copy
import com.w2sv.wifiwidget.ui.designsystem.ElevatedIconHeaderCard
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.util.PreviewOf

private val checkRowColumnBottomPadding = 8.dp
private val verticalItemSpacing = 16.dp
private val widgetConfigCardInnerPadding = PaddingValues(vertical = 18.dp)

private val listContentPadding: PaddingValues
    @Composable
    get() = PaddingValues(
        bottom = if (isPortraitModeActive) 140.dp else 90.dp, // for FABs
        top = verticalItemSpacing
    )

typealias UpdateWidgetConfig = (WifiWidgetConfig.() -> WifiWidgetConfig) -> Unit

@Composable
fun WidgetConfigList(
    state: LazyListState,
    config: WifiWidgetConfig,
    updateConfig: UpdateWidgetConfig,
    showDialog: (WidgetConfigDialog) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = state,
        modifier = modifier.padding(horizontal = if (isPortraitModeActive) 26.dp else 126.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(verticalItemSpacing),
        contentPadding = listContentPadding
    ) {
        item {
            AppearanceConfigCard(
                appearance = config.appearance,
                updateAppearance = { updateAppearance -> updateConfig { copy(appearance = updateAppearance(appearance)) } },
                showDialog = showDialog
            )
        }
        item {
            WifiPropertiesConfigCard(
                config = config,
                updateConfig = updateConfig,
                showDialog = showDialog
            )
        }
        item {
            UtilitiesConfigCard(
                isEnabled = { config.utilities.getValue(it) },
                update = { property, isEnabled ->
                    updateConfig { copy(utilities = utilities.copy { put(property, isEnabled) }) }
                },
                modifier = Modifier.padding(bottom = checkRowColumnBottomPadding)
            )
        }
        item {
            RefreshingConfigCard(
                refreshing = config.refreshing,
                updateRefreshing = { updateRefreshing -> updateConfig { copy(refreshing = updateRefreshing(refreshing)) } },
                showDialog = showDialog,
                modifier = Modifier.padding(bottom = checkRowColumnBottomPadding)
            )
        }
    }
}

@Preview
@Composable
private fun Prev() {
    PreviewOf {
        WidgetConfigList(
            rememberLazyListState(),
            WifiWidgetConfig.default,
            {},
            {}
        )
    }
}

@Composable
fun WidgetConfigSectionCard(
    header: IconHeader,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedIconHeaderCard(
        iconHeader = header,
        innerPadding = widgetConfigCardInnerPadding,
        modifier = modifier,
        content = content
    )
}
