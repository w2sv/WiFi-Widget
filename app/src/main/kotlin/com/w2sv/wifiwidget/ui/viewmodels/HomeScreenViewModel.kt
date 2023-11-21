package com.w2sv.wifiwidget.ui.viewmodels

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.eventhandling.BackPressHandler
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.data.model.WifiProperty
import com.w2sv.data.networking.WifiStatusMonitor
import com.w2sv.data.repositories.PreferencesRepository
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionUIState
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.WifiStatusUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository,
    @ApplicationContext context: Context,
    wifiPropertyValueGetterResourcesProvider: WidgetWifiProperty.ValueGetterResources.Provider,
    wifiStatusMonitor: WifiStatusMonitor,
) : ViewModel() {

    fun onStart(context: Context) {
        wifiStatusUIState.triggerPropertiesViewDataRefresh()
        lapUIState.updateBackgroundAccessGranted(context = context)
    }

    val showWidgetConfigurationDialog = MutableStateFlow(false)

    // ===================
    // Snackbar
    // ===================

    val snackbarHostState = SnackbarHostState()

    fun showSnackbar(visuals: SnackbarVisuals) {
        viewModelScope.launch {
            snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(visuals)
        }
    }

    // ===================
    // UI State Objects
    // ===================

    val lapUIState = LocationAccessPermissionUIState(
        preferencesRepository = preferencesRepository,
        snackbarHostState = snackbarHostState,
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
        wifiPropertyValueGetterResourcesProvider,
        wifiStatusMonitor,
        viewModelScope,
    )

    // ==============
    // BackPress Handling
    // ==============

    val exitApplication = MutableSharedFlow<Unit>()

    fun onBackPress(context: Context) {
        backPressHandler.invoke(
            onFirstPress = {
                context.showToast(context.getString(R.string.tap_again_to_exit))
            },
            onSecondPress = {
                viewModelScope.launch {
                    exitApplication.emit(Unit)
                }
            },
        )
    }

    private val backPressHandler = BackPressHandler(
        viewModelScope,
        2500L,
    )
}
