package com.w2sv.wifiwidget.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.ui.UnconfirmedStateFlow
import com.w2sv.androidutils.ui.UnconfirmedStateMap
import com.w2sv.androidutils.ui.UnconfirmedStatesComposition
import com.w2sv.data.model.Theme
import com.w2sv.data.model.WifiProperty
import com.w2sv.data.storage.WidgetRepository
import com.w2sv.wifiwidget.ui.utils.getSynchronousMutableStateMap
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

    val infoDialogProperty: MutableStateFlow<WifiProperty?> = MutableStateFlow(null)

    // =========
    // Configuration
    // =========

    val setWifiProperties =
        UnconfirmedStateMap(
            coroutineScope = viewModelScope,
            appliedFlowMap = repository.wifiProperties,
            makeSynchronousMutableMap = { it.getSynchronousMutableStateMap() },
            syncState = { repository.saveMap(it) }
        )

    val refreshingParametersMap by lazy {
        UnconfirmedStateMap(
            coroutineScope = viewModelScope,
            appliedFlowMap = repository.refreshingParametersMap,
            makeSynchronousMutableMap = { it.getSynchronousMutableStateMap() },
            syncState = {
                repository.saveMap(it)
                refreshingParametersChanged.emit(Unit)
            }
        )
    }

    val theme = UnconfirmedStateFlow(
        coroutineScope = viewModelScope,
        appliedFlow = repository.theme,
        syncState = {
            repository.saveTheme(it)
        }
    )

    val customColorsMap =
        UnconfirmedStateMap(
            coroutineScope = viewModelScope,
            appliedFlowMap = repository.customColorsMap,
            makeSynchronousMutableMap = { it.getSynchronousMutableStateMap() },
            syncState = { repository.saveMap(it) }
        )

    val opacity = UnconfirmedStateFlow(
        coroutineScope = viewModelScope,
        appliedFlow = repository.opacity,
        syncState = { repository.saveOpacity(it) }
    )

    val configuration = UnconfirmedStatesComposition(
        unconfirmedStates = listOf(
            setWifiProperties,
            theme,
            customColorsMap,
            opacity,
            refreshingParametersMap
        ),
        coroutineScope = viewModelScope
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