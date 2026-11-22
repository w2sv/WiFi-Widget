package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.kotlinutils.copy
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.util.PreviewOf
import sh.calvin.reorderable.rememberReorderableLazyListState

@Preview(showBackground = true)
@Composable
private fun Prev() {
    PreviewOf {
        WidgetConfigList(
            rememberLazyListState(),
            WidgetConfig.default,
            {},
            {}
        )
    }
}

@Composable
fun WidgetConfigList(
    state: LazyListState,
    config: WidgetConfig,
    updateConfig: UpdateWidgetConfig,
    showDialog: (WidgetConfigDialog) -> Unit,
    modifier: Modifier = Modifier
) {
    val propertyByKey = config.supportedProperties.associateBy { it.protoId }
    val reorderableState = rememberReorderableLazyListState(state) { from, to ->
        val source = propertyByKey.getValue(from.key as Int)
        val destination = propertyByKey.getValue(to.key as Int)
        updateConfig { withMovedSupportedProperty(source, destination) }
    }

    LazyColumn(
        state = state,
        modifier = modifier.testTag("widget_config_list"),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = WidgetConfigListDimens.screenContentPadding
    ) {
        item(key = "appearance") {
            AppearanceConfigCard(
                appearance = config.appearance,
                updateAppearance = { updateAppearance -> updateConfig { copy(appearance = updateAppearance(appearance)) } },
                showDialog = showDialog,
                modifier = Modifier.padding(bottom = WidgetConfigListDimens.sectionSpacing)
            )
        }
        item(key = "wifi_properties_header") {
            WifiPropertiesConfigHeader(
                config = config,
                updateConfig = updateConfig
            )
        }
        reorderableWifiPropertyItems(
            config = config,
            updateConfig = updateConfig,
            showDialog = showDialog,
            reorderableState = reorderableState
        )
        item(key = "utilities") {
            UtilitiesConfigList(
                isEnabled = { config.utilities.getValue(it) },
                update = { property, isEnabled ->
                    updateConfig { copy(utilities = utilities.copy { put(property, isEnabled) }) }
                },
                modifier = Modifier.padding(bottom = WidgetConfigListDimens.sectionSpacing)
            )
        }
        item(key = "refreshing") {
            RefreshingConfigCard(
                refreshing = config.refreshing,
                updateRefreshing = { updateRefreshing -> updateConfig { copy(refreshing = updateRefreshing(refreshing)) } },
                showDialog = showDialog
            )
        }
    }
}
