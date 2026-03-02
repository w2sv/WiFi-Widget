package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.isPortraitModeActive
import com.w2sv.domain.model.widget.WidgetBottomBarElement
import com.w2sv.domain.model.widget.WifiWidgetConfig
import com.w2sv.kotlinutils.copy
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.ElevatedIconHeaderCard
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.screen.widgetconfig.list.appearance.AppearanceConfigCard
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import kotlinx.collections.immutable.toPersistentList

private val verticalCardSpacing = 16.dp
private val checkRowColumnBottomPadding = 8.dp
private val innerWidgetConfigurationCardPadding = PaddingValues(vertical = 18.dp)

typealias UpdateWidgetConfig = (WifiWidgetConfig.() -> WifiWidgetConfig) -> Unit

@Composable
fun WidgetConfigList(
    config: WifiWidgetConfig,
    updateConfig: UpdateWidgetConfig,
    showDialog: (WidgetConfigDialog) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = if (isPortraitModeActive) 26.dp else 126.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
            BottomBarConfigCard(
                { config.bottomBarElements.getValue(it) },
                update = { property, isEnabled ->
                    updateConfig {
                        copy(bottomBarElements = bottomBarElements.copy {
                            put(
                                property,
                                isEnabled
                            )
                        })
                    }
                },
                modifier = Modifier.padding(bottom = checkRowColumnBottomPadding)
            )
        }
        item {
            RefreshingConfigCard(
                refreshing = { config.refreshing },
                update = { updateRefreshing -> updateConfig { copy(refreshing = updateRefreshing(refreshing)) } },
                showDialog = showDialog,
                modifier = Modifier.padding(bottom = checkRowColumnBottomPadding)
            )
        }
    }
}

@Composable
private fun BottomBarConfigCard(
    isEnabled: (WidgetBottomBarElement) -> Boolean,
    update: (WidgetBottomBarElement, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    WidgetConfigSectionCard(
        header = IconHeader(
            iconRes = R.drawable.ic_bottom_row_24,
            stringRes = R.string.bottom_bar
        )
    ) {
        CheckRowColumn(
            elements = remember {
                WidgetBottomBarElement.entries.map { element ->
                    ConfigListElement.CheckRow(
                        property = element,
                        isChecked = { isEnabled(element) },
                        onCheckedChange = { update(element, it) },
                        explanation = element.explanation
                    )
                }
                    .toPersistentList()
            },
            modifier = modifier
        )
    }
}

@Composable
fun WidgetConfigSectionCard(header: IconHeader, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    ElevatedIconHeaderCard(
        iconHeader = header,
        innerPadding = innerWidgetConfigurationCardPadding,
        modifier = modifier,
        content = content
    )
}
