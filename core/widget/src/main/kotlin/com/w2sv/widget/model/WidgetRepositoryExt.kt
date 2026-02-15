package com.w2sv.widget.model

import com.w2sv.common.utils.mapFlow
import com.w2sv.domain.repository.WidgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal fun WidgetRepository.widgetAppearance(): Flow<WidgetAppearance> =
    combine(
        coloringConfig,
        opacity,
        fontSize,
        propertyValueAlignment,
        bottomRowElementEnablementMap.mapFlow()
    ) { t1, t2, t3, t4, t5 ->
        WidgetAppearance(
            coloringConfig = t1,
            backgroundOpacity = t2,
            fontSize = t3,
            propertyValueAlignment = t4,
            bottomBar = WidgetBottomBarElement(t5)
        )
    }
