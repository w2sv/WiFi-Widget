package com.w2sv.wifiwidget.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import com.w2sv.wifiwidget.preferences.WidgetPreferences

class HomeScreenViewModel : ViewModel() {
    val propertyKey2State: SnapshotStateMap<String, Boolean> = mutableStateMapOf(
        *WidgetPreferences
            .keys
            .map { it to WidgetPreferences.getValue(it) }.toTypedArray()
    )

    /**
     * @return flag indicating whether any property has been updated
     */
    fun syncWidgetProperties(): Boolean {
        var updatedProperty = false
        propertyKey2State.forEach { (k, v) ->
            if (v != WidgetPreferences.getValue(k)) {
                WidgetPreferences[k] = v
                updatedProperty = true
            }
        }

        return updatedProperty
    }
}