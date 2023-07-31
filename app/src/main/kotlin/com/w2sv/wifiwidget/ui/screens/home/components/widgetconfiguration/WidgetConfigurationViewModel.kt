package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration

import com.w2sv.androidutils.ui.PreferencesDataStoreBackedUnconfirmedStatesViewModel
import com.w2sv.common.data.storage.WidgetConfigurationRepository
import com.w2sv.common.data.model.Theme
import com.w2sv.common.data.model.WifiProperty
import com.w2sv.common.extensions.getSynchronousMutableStateMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

@HiltViewModel
class WidgetConfigurationViewModel @Inject constructor(
    repository: WidgetConfigurationRepository,
) : PreferencesDataStoreBackedUnconfirmedStatesViewModel<WidgetConfigurationRepository>(repository) {

    // ==========
    // Dialog
    // ==========

    fun onDismissWidgetConfigurationDialog() {
        nonAppliedWidgetConfiguration.launchReset()
    }

    // ========================
    // Overlay dialogs
    // ========================

    val infoDialogProperty: MutableStateFlow<WifiProperty?> = MutableStateFlow(null)

    // =========
    // NonAppliedState
    // =========

    val nonAppliedWidgetConfiguration by lazy {
        makeUnconfirmedStatesComposition(
            listOf(
                nonAppliedWifiPropertyFlags,
                nonAppliedWidgetTheme,
                nonAppliedWidgetOpacity,
                nonAppliedWidgetRefreshingParameterFlags,
                nonAppliedWidgetColors
            )
        )
    }

    // =========
    // NonAppliedSnapshotStateMap
    // =========

    val nonAppliedWifiPropertyFlags by lazy {
        makeUnconfirmedStateMap(
            repository.wifiProperties,
            makeMutableMap = { it.getSynchronousMutableStateMap() }
        )
    }

    val nonAppliedWidgetColors by lazy {
        makeUnconfirmedStateMap(
            repository.customColors,
            makeMutableMap = { it.getSynchronousMutableStateMap() }
        )
    }

    val nonAppliedWidgetRefreshingParameterFlags by lazy {
        makeUnconfirmedStateMap(
            repository.refreshingParameters,
            makeMutableMap = { it.getSynchronousMutableStateMap() },
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

    val customThemeSelected = nonAppliedWidgetTheme
        .transform {
            emit(it == Theme.Custom)
        }

    val widgetRefreshingParametersChanged = MutableSharedFlow<Unit>()
}