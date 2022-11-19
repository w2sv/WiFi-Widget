package com.w2sv.wifiwidget.screens.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.w2sv.wifiwidget.preferences.WidgetPreferences

class HomeScreenViewModel : ViewModel() {
    val propertyStates =
        WidgetPreferences
            .keys
            .associateWith { mutableStateOf(WidgetPreferences.getValue(it)) }
            .toMutableMap()

    fun anyWidgetPropertiesChanged(): Boolean {
        var updatedProperty = false
        propertyStates.forEach { (k, v) ->
            if (v.value != WidgetPreferences.getValue(k)) {
                WidgetPreferences[k] = v.value
                updatedProperty = true
            }
        }

        return updatedProperty
    }
}