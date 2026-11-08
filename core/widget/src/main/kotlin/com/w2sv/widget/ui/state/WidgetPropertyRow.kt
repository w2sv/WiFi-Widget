package com.w2sv.widget.ui.state

import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentation

internal data class WidgetPropertyRow(val id: Long, val presentation: WifiPropertyPresentation) {
    constructor(property: WifiProperty, presentation: WifiPropertyPresentation, propertyIndex: Int = 0) : this(
        property.rowId(propertyIndex),
        presentation
    )
}

private fun WifiProperty.rowId(index: Int = 0): Long =
    protoId.toLong() * 1_000 + index
