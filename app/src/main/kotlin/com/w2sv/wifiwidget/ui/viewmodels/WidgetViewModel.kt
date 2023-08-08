package com.w2sv.wifiwidget.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.data.model.Theme
import com.w2sv.data.storage.WidgetRepository
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.InfoDialogData
import com.w2sv.wifiwidget.ui.utils.getUnconfirmedStateFlow
import com.w2sv.wifiwidget.ui.utils.getUnconfirmedStateMap
import com.w2sv.wifiwidget.ui.utils.getUnconfirmedStatesComposition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetViewModel @Inject constructor(private val repository: WidgetRepository) :
    ViewModel() {

    // ==========
    // Dialog
    // ==========

    fun onDismissWidgetConfigurationDialog() {
        viewModelScope.launch {
            configuration.reset()
        }
    }

    // ========================
    // Overlay dialogs
    // ========================

    val infoDialogData: MutableStateFlow<InfoDialogData?> = MutableStateFlow(null)

    // =========
    // Configuration
    // =========

    val wifiProperties by lazy {
        getUnconfirmedStateMap(
            appliedFlowMap = repository.wifiProperties,
            syncState = { repository.saveMap(it) }
        )
    }

    val subWifiProperties by lazy {
        getUnconfirmedStateMap(
            repository.subWifiProperties,
            syncState = { repository.saveMap(it) }
        )
    }

    val buttonMap by lazy {
        getUnconfirmedStateMap(
            appliedFlowMap = repository.buttonMap,
            syncState = {
                repository.saveMap(it)
            }
        )
    }

    val refreshingParametersMap by lazy {
        getUnconfirmedStateMap(
            appliedFlowMap = repository.refreshingParametersMap,
            syncState = {
                repository.saveMap(it)
                refreshingParametersChanged.emit(Unit)
            }
        )
    }

    val useDynamicColors = getUnconfirmedStateFlow(
        appliedFlow = repository.useDynamicColors,
        syncState = {
            repository.savUseDynamicColors(it)
        }
    )

    val theme = getUnconfirmedStateFlow(
        appliedFlow = repository.theme,
        syncState = {
            repository.saveTheme(it)
        }
    )

    val customColorsMap =
        getUnconfirmedStateMap(
            appliedFlowMap = repository.customColorsMap,
            syncState = { repository.saveMap(it) }
        )

    val opacity = getUnconfirmedStateFlow(
        appliedFlow = repository.opacity,
        syncState = { repository.saveOpacity(it) }
    )

    val configuration = getUnconfirmedStatesComposition(
        unconfirmedStates = listOf(
            useDynamicColors,
            theme,
            customColorsMap,
            opacity,
            wifiProperties,
            subWifiProperties,
            buttonMap,
            refreshingParametersMap
        )
    )

    // ===========================
    // State change side effects
    // ===========================

    val customThemeSelected = theme
        .transform {
            emit(it == Theme.Custom)
        }

    val refreshingParametersChanged = MutableSharedFlow<Unit>()
}