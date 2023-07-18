package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration

import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.coroutines.reset
import com.w2sv.androidutils.ui.PreferencesDataStoreBackedUnconfirmedStatesViewModel
import com.w2sv.androidutils.ui.UnconfirmedStatesComposition
import com.w2sv.common.data.repositories.WidgetConfigurationRepository
import com.w2sv.common.enums.Theme
import com.w2sv.common.enums.WidgetColorSection
import com.w2sv.common.enums.WifiProperty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetConfigurationViewModel @Inject constructor(
    repository: WidgetConfigurationRepository,
) : PreferencesDataStoreBackedUnconfirmedStatesViewModel<WidgetConfigurationRepository>(repository) {

    // ==========
    // Dialog
    // ==========

    fun onDismissWidgetConfigurationDialog() {
        viewModelScope.launch {
            nonAppliedWidgetConfiguration.reset()
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
        UnconfirmedStatesComposition(
            listOf(
                nonAppliedWifiPropertyFlags,
                nonAppliedWidgetTheme,
                nonAppliedWidgetOpacity,
                nonAppliedWidgetRefreshingParameterFlags,
                nonAppliedWidgetColors
            ),
            coroutineScope = viewModelScope
        )
    }

    // =========
    // NonAppliedSnapshotStateMap
    // =========

    val nonAppliedWifiPropertyFlags by lazy {
        makeUnconfirmedStateMap(repository.wifiProperties)
    }

    val nonAppliedWidgetColors by lazy {
        makeUnconfirmedStateMap(repository.customColors)
    }

    val nonAppliedWidgetRefreshingParameterFlags by lazy {
        makeUnconfirmedStateMap(
            repository.refreshingParameters,
            onStateSynced = {
                widgetRefreshingParametersChanged.emit(Unit)
            }
        )
    }

    // =====================
    // NonAppliedStateFlow
    // =====================

    val nonAppliedWidgetTheme = makeUnconfirmedEnumValuedStateFlow(
        appliedFlow = repository.theme,
        preferencesKey = WidgetConfigurationRepository.Key.WIDGET_THEME,
        onStateSynced = {}
    )

    val nonAppliedWidgetOpacity =
        makeUnconfirmedStateFlow(repository.opacity, WidgetConfigurationRepository.Key.OPACITY)

    // ===========================
    // State change side effects
    // ===========================

    val customThemeSelected = nonAppliedWidgetTheme.transform {
        emit(it == Theme.Custom)
    }

    val widgetRefreshingParametersChanged = MutableSharedFlow<Unit>()
}