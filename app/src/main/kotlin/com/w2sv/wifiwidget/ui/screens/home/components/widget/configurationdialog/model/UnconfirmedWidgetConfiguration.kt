package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateFlow
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStatesComposition
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetButton
import com.w2sv.domain.model.WidgetColor
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.transform

class UnconfirmedWidgetConfiguration(
    val wifiProperties: UnconfirmedStateMap<WidgetWifiProperty, Boolean>,
    val subWifiProperties: UnconfirmedStateMap<WidgetWifiProperty.IPProperty.V4AndV6, WidgetWifiProperty.IPProperty.V4AndV6.EnabledTypes>,
    val buttonMap: UnconfirmedStateMap<WidgetButton, Boolean>,
    val refreshingParametersMap: UnconfirmedStateMap<WidgetRefreshingParameter, Boolean>,
    val useDynamicColors: UnconfirmedStateFlow<Boolean>,
    val theme: UnconfirmedStateFlow<Theme>,
    val customColorsMap: UnconfirmedStateMap<WidgetColor, Int>,
    val opacity: UnconfirmedStateFlow<Float>,
    scope: CoroutineScope,
) : UnconfirmedStatesComposition(
    unconfirmedStates = listOf(
        wifiProperties,
        subWifiProperties,
        buttonMap,
        refreshingParametersMap,
        useDynamicColors,
        theme,
        customColorsMap,
        opacity,
    ),
    coroutineScope = scope,
) {
    val customThemeSelected = theme
        .transform {
            emit(it == Theme.Custom)
        }
}
