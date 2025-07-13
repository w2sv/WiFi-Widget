package com.w2sv.widget.data

import com.w2sv.common.di.AppIoScope
import com.w2sv.common.utils.mapFlow
import com.w2sv.domain.model.WidgetWifiState
import com.w2sv.domain.model.WifiProperty
import com.w2sv.domain.model.WifiStatus
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.kotlinutils.coroutines.flow.stateInWithBlockingInitial
import com.w2sv.networking.WifiStatusGetter
import com.w2sv.widget.model.TopBar
import com.w2sv.widget.model.WidgetAppearance
import com.w2sv.widget.model.WidgetRefreshing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WidgetModuleWidgetRepository @Inject constructor(
    widgetRepository: WidgetRepository,
    private val viewDataFactory: WifiProperty.ViewData.Factory,
    @param:AppIoScope private val scope: CoroutineScope,
    private val wifiStatusGetter: WifiStatusGetter
) : WidgetRepository by widgetRepository {

    val widgetAppearance = combine(
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
                topBar = TopBar(t5),
                showLastRefreshTime = true // TODO
            )
        }
    )

    val refreshing by lazy {
        combine(
            refreshingParametersEnablementMap.mapFlow(),
            refreshInterval
        ) { t1, t2 -> WidgetRefreshing(t1, t2) }
            .stateInWithBlockingInitial(scope)
    }

    val widgetState get() = _widgetState.asStateFlow()
    private val _widgetState = MutableStateFlow<WidgetWifiState>(WidgetWifiState.Disconnected)

    fun updateState() {
        _widgetState.update {
            when (wifiStatusGetter()) {
                WifiStatus.Disabled -> WidgetWifiState.Disabled
                WifiStatus.Disconnected -> WidgetWifiState.Disconnected
                WifiStatus.Connected -> WidgetWifiState.Connected.PropertiesLoading.also {
                    scope.launch {
                        _widgetState.update { WidgetWifiState.Connected.PropertiesAvailable(viewData().toList()) }
                    }
                }
            }
        }
    }

    suspend fun viewData(): Flow<WifiProperty.ViewData> {
        val subProperties = enabledIpSubProperties.first()
        val locationParams = enabledLocationParameters.first()
        return viewDataFactory.invoke(
            properties = sortedEnabledWifiProperties.first(),
            getIpSubProperties = { subProperties },
            getLocationParameters = { locationParams }
        )
    }
}
