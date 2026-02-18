package com.w2sv.widget.layout

import android.content.Context
import com.w2sv.domain.model.widget.WidgetAppearance

internal fun WidgetAppearance.resolvedWidgetColors(context: Context): WidgetColors =
    WidgetColors.fromStyle(coloringConfig.appliedStyle, context)
