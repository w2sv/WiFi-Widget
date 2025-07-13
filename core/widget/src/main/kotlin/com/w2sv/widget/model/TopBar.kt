package com.w2sv.widget.model

import com.w2sv.domain.model.WidgetBottomBarElement

internal data class TopBar(val elements: List<WidgetBottomBarElement>) {

    val show: Boolean = elements.isNotEmpty()

    constructor(enablementMap: Map<WidgetBottomBarElement, Boolean>) :
        this(
            elements = WidgetBottomBarElement.entries.mapNotNull {
                if (enablementMap.getValue(it)) it else null
            }
        )
}
