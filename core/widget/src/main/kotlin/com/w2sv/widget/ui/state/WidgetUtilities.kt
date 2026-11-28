package com.w2sv.widget.ui.state

import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetUtility
import com.w2sv.widget.ui.actions.WidgetButtonAction

internal data class WidgetUtilities(val showRefreshTime: Boolean, val actions: List<WidgetButtonAction>) {
    constructor(widgetConfig: WidgetConfig) : this(
        widgetConfig.utilities[WidgetUtility.RefreshTimeDisplay] == true,
        WidgetButtonAction.entries.filter { widgetConfig.utilities[it.configurationKey] == true }
    )
}
