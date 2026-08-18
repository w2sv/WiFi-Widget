package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.composed.ui.platform.isPortraitModeActive
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.kotlinutils.copy
import com.w2sv.wifiwidget.ui.designsystem.ElevatedIconHeaderCard
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.util.PreviewOf

private object Dimens {
    val verticalSpacing = 16.dp
    val cardInnerPadding = PaddingValues(vertical = 18.dp)

    val contentPadding: PaddingValues
        @Composable
        @ReadOnlyComposable
        get() {
            val horizontalPadding = if (isPortraitModeActive) 26.dp else 126.dp
            return PaddingValues(
                bottom = if (isPortraitModeActive) 140.dp else 90.dp, // for FABs
                top = verticalSpacing,
                start = horizontalPadding,
                end = horizontalPadding
            )
        }
}

typealias UpdateWidgetConfig = (WidgetConfig.() -> WidgetConfig) -> Unit

@Composable
fun WidgetConfigList(
    state: LazyListState,
    config: WidgetConfig,
    updateConfig: UpdateWidgetConfig,
    showDialog: (WidgetConfigDialog) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = state,
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.verticalSpacing),
        contentPadding = Dimens.contentPadding
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
                }
            )
        }
        item {
            RefreshingConfigCard(
                refreshing = config.refreshing,
                updateRefreshing = { updateRefreshing -> updateConfig { copy(refreshing = updateRefreshing(refreshing)) } },
                showDialog = showDialog
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
            WidgetConfig.default,
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
        innerPadding = Dimens.cardInnerPadding,
        modifier = modifier,
        content = content
    )
}
