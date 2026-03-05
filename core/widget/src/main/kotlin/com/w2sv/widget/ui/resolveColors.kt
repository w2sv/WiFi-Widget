package com.w2sv.widget.ui

import android.content.Context
import com.w2sv.domain.model.widget.WidgetAppearance

internal fun WidgetAppearance.resolveColors(context: Context): WidgetColors =
    WidgetColors.fromStyle(coloringConfig.appliedStyle, context)
