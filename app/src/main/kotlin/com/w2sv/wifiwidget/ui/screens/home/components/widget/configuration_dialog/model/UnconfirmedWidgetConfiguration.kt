package com.w2sv.wifiwidget.ui.screens.home.components.widget.configuration_dialog.model

import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateFlow
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStatesComposition
import com.w2sv.data.model.Theme
import com.w2sv.data.model.WifiProperty
import com.w2sv.data.model.widget.WidgetButton
import com.w2sv.data.model.widget.WidgetColor
import com.w2sv.data.model.widget.WidgetRefreshingParameter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.transform

class UnconfirmedWidgetConfiguration(
    val wifiProperties: UnconfirmedStateMap<WifiProperty, Boolean>,
    val subWifiProperties: UnconfirmedStateMap<WifiProperty.IPProperty.SubProperty, Boolean>,
    val buttonMap: UnconfirmedStateMap<WidgetButton, Boolean>,
    val refreshingParametersMap: UnconfirmedStateMap<WidgetRefreshingParameter, Boolean>,
    val useDynamicColors: UnconfirmedStateFlow<Boolean>,
    val theme: UnconfirmedStateFlow<Theme>,
    val customColorsMap: UnconfirmedStateMap<WidgetColor, Int>,
    val opacity: UnconfirmedStateFlow<Float>,
    scope: CoroutineScope
) : UnconfirmedStatesComposition(
    unconfirmedStates = listOf(
        wifiProperties,
        subWifiProperties,
        buttonMap,
        refreshingParametersMap,
        useDynamicColors,
        theme,
        customColorsMap,
        opacity
    ),
    coroutineScope = scope
) {
    val customThemeSelected = theme
        .transform {
            emit(it == Theme.Custom)
        }
}