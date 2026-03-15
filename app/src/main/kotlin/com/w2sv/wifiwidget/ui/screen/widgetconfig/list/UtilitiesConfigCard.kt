package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.w2sv.core.common.R
import com.w2sv.domain.model.widget.WidgetUtility
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.designsystem.configlist.CheckRowColumn
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigListElement
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
        CheckRowColumn(
            elements = WidgetUtility.entries.map { element ->
                ConfigListElement.CheckRow(
                    property = element,
                    isChecked = { isEnabled(element) },
                    onCheckedChange = { update(element, it) },
                    explanation = element.explanation
                )
            }
                .toPersistentList(),
            modifier = modifier
        )
    }
}
