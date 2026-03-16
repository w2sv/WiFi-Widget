package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.w2sv.core.common.R
import com.w2sv.domain.model.widget.WidgetUtility
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.designsystem.configlist.CheckableList
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigItem
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigListToken
import kotlinx.collections.immutable.toPersistentList

@Composable
fun UtilitiesConfigCard(
    isEnabled: (WidgetUtility) -> Boolean,
    update: (WidgetUtility, Boolean) -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    WidgetConfigSectionCard(
        header = IconHeader(
            iconRes = R.drawable.ic_build_24,
            stringRes = R.string.utilities
        )
    ) {
        CheckableList(
            elements = WidgetUtility.entries.map { element ->
                ConfigItem.Checkable(
                    property = element,
                    isChecked = { isEnabled(element) },
                    onCheckedChange = { update(element, it) },
                    contentBeneath = ConfigItem.Explanation(element.explanation)
                )
            }
                .toPersistentList(),
            modifier = modifier,
            arrangement = Arrangement.spacedBy(ConfigListToken.itemSpacing)
        )
    }
}
