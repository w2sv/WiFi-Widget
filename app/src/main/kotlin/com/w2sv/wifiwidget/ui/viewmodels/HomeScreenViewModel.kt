package com.w2sv.wifiwidget.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.data.repositories.PreferencesRepository
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.networking.WifiStatusMonitor
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionState
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.WifiStatusUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository,
    @ApplicationContext context: Context,
    widgetWifiPropertyValueGetter: WidgetWifiProperty.ValueGetter,
    wifiStatusMonitor: WifiStatusMonitor,
) : ViewModel() {

    fun onStart(context: Context) {
        wifiStatusUIState.triggerPropertiesViewDataRefresh()
        lapUIState.updateBackgroundAccessGranted(context = context)
    }

    val showWidgetConfigurationDialog get() = _showWidgetConfigurationDialog.asStateFlow()
    private val _showWidgetConfigurationDialog = MutableStateFlow(false)

    fun setShowWidgetConfigurationDialog(value: Boolean) {
        _showWidgetConfigurationDialog.value = value
    }

    // ===================
    // UI State Objects
    // ===================

    val lapUIState = LocationAccessPermissionState(
        preferencesRepository = preferencesRepository,
        scope = viewModelScope,
        context = context,
    )
        .apply {
            viewModelScope.launch {
                newlyGranted.collect {
                    if (it) {
                        wifiStatusUIState.triggerPropertiesViewDataRefresh()
                    }
                }
            }
        }

    val wifiStatusUIState = WifiStatusUIState(
        widgetWifiPropertyValueGetter,
        wifiStatusMonitor,
        viewModelScope,
    )
}
