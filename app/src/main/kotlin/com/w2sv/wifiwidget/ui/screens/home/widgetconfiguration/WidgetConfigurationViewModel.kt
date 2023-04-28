package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.coroutines.reset
import com.w2sv.common.enums.Theme
import com.w2sv.common.enums.WidgetColorSection
import com.w2sv.common.enums.WifiProperty
import com.w2sv.common.datastore.DataStoreRepository
import com.w2sv.common.datastore.DataStoreRepositoryInterfacingViewModel
import com.w2sv.common.datastore.PreferencesKey
import com.w2sv.widget.WidgetProvider
import com.w2sv.wifiwidget.ui.CoherentNonAppliedStates
import com.w2sv.wifiwidget.ui.NonAppliedSnapshotStateMap
import com.w2sv.wifiwidget.ui.NonAppliedStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetConfigurationViewModel @Inject constructor(
    dataStoreRepository: DataStoreRepository,
    private val savedStateHandle: SavedStateHandle
) : DataStoreRepositoryInterfacingViewModel(dataStoreRepository) {

    // ===================================
    // onSplashScreenAnimationFinished
    // ===================================

    fun onSplashScreenAnimationFinished() {
        if (savedStateHandle.contains(WidgetProvider.EXTRA_OPEN_CONFIGURATION_DIALOG_ON_START)) {
            showWidgetConfigurationDialog.value = true
        }
    }

    // ==========
    // Dialog
    // ==========

    val showWidgetConfigurationDialog = MutableStateFlow(false)

    fun onDismissWidgetConfigurationDialog() {
        viewModelScope.launch {
            nonAppliedWidgetConfiguration.reset()
            showWidgetConfigurationDialog.value = false
        }
    }

    // ========================
    // Dialog overlay dialogs
    // ========================

    val infoDialogProperty: MutableStateFlow<WifiProperty?> = MutableStateFlow(null)

    val customizationDialogSection = MutableStateFlow<WidgetColorSection?>(null)

    fun onDismissCustomizationDialog() {
        customizationDialogSection.reset()
    }

    // =========
    // NonAppliedState
    // =========

    val nonAppliedWidgetConfiguration by lazy {
        CoherentNonAppliedStates(
            nonAppliedWifiPropertyFlags,
            nonAppliedWidgetTheme,
            nonAppliedWidgetOpacity,
            nonAppliedWidgetRefreshingParameterFlags,
            nonAppliedWidgetColors,
            coroutineScope = viewModelScope
        )
    }

    // =========
    // NonAppliedSnapshotStateMap
    // =========

    val nonAppliedWifiPropertyFlags by lazy {
        NonAppliedSnapshotStateMap(
            viewModelScope,
            dataStoreRepository.wifiProperties,
            dataStoreRepository
        )
    }

    val nonAppliedWidgetColors by lazy {
        NonAppliedSnapshotStateMap(
            viewModelScope,
            dataStoreRepository.customWidgetColors,
            dataStoreRepository
        )
    }

    val nonAppliedWidgetRefreshingParameterFlags by lazy {
        NonAppliedSnapshotStateMap(
            viewModelScope,
            dataStoreRepository.widgetRefreshingParameters,
            dataStoreRepository
        ) {
            viewModelScope.launch {
                widgetRefreshingParametersChanged.emit(Unit)
            }
        }
    }

    // =====================
    // NonAppliedStateFlow
    // =====================

    val nonAppliedWidgetTheme = NonAppliedStateFlow(
        viewModelScope,
        dataStoreRepository.widgetTheme
    ) {
        dataStoreRepository.save(PreferencesKey.WIDGET_THEME, it)
    }

    val nonAppliedWidgetOpacity = NonAppliedStateFlow(
        viewModelScope,
        dataStoreRepository.opacity
    ) {
        dataStoreRepository.save(PreferencesKey.OPACITY, it)
    }

    // ===========================
    // State change side effects
    // ===========================

    val customThemeSelected = nonAppliedWidgetTheme.transform {
        emit(it == Theme.Custom)
    }

    val widgetRefreshingParametersChanged = MutableSharedFlow<Unit>()
}