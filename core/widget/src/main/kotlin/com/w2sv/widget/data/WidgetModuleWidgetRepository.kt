package com.w2sv.widget.data

import com.w2sv.common.di.AppIoScope
import com.w2sv.common.utils.mapFlow
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.kotlinutils.coroutines.flow.stateInWithBlockingInitial
import com.w2sv.widget.model.WidgetAppearance
import com.w2sv.widget.model.WidgetBottomBarElement
import com.w2sv.widget.model.WidgetRefreshing
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine

@Singleton
internal class WidgetModuleWidgetRepository @Inject constructor(
    widgetRepository: WidgetRepository,
    @param:AppIoScope private val scope: CoroutineScope
) : WidgetRepository by widgetRepository {

    val widgetAppearance by lazy {
        combine(
            coloringConfig,
            opacity,
            fontSize,
            propertyValueAlignment,
            bottomRowElementEnablementMap.mapFlow(),
            transform = { t1, t2, t3, t4, t5 ->
                WidgetAppearance(
                    coloringConfig = t1,
                    backgroundOpacity = t2,
                    fontSize = t3,
                    propertyValueAlignment = t4,
                    bottomBar = WidgetBottomBarElement(t5)
                )
            }
        )
            .stateInWithBlockingInitial(scope)
    }

    val refreshing by lazy {
        combine(
            refreshingParametersEnablementMap.mapFlow(),
            refreshInterval
        ) { t1, t2 -> WidgetRefreshing(t1, t2) }
            .stateInWithBlockingInitial(scope)
    }
}
