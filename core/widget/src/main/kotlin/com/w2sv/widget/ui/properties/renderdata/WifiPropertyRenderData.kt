package com.w2sv.widget.ui.properties.renderdata

import androidx.annotation.LayoutRes
import com.w2sv.domain.widget.FontSize
import com.w2sv.domain.widget.WidgetColors
import com.w2sv.domain.wifiproperty.viewdata.WifiPropertyViewData

internal data class WifiPropertyRenderData(
    val viewData: List<WifiPropertyViewData>,
    val colors: WidgetColors,
    val fontSize: FontSize,
    @LayoutRes val layout: Int
)
