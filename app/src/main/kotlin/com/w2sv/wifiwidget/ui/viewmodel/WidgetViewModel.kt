package com.w2sv.wifiwidget.ui.viewmodel

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.material3.SnackbarVisuals
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.common.utils.log
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.reversiblestate.ReversibleStateFlow
import com.w2sv.reversiblestate.datastore.reversibleStateFlow
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.widget.WifiWidgetRefreshWorker
import com.w2sv.widget.di.WidgetPinSuccessFlow
import com.w2sv.widget.utils.attemptWifiWidgetPin
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.di.MakeSnackbarVisualsFlow
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.ReversibleWidgetConfiguration
import com.w2sv.wifiwidget.ui.utils.reversibleStateMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetViewModel @Inject constructor(
    private val repository: WidgetRepository,
    private val widgetDataRefreshWorkerManager: WifiWidgetRefreshWorker.Manager,
    private val appWidgetManager: AppWidgetManager,
    private val preferencesRepository: PreferencesRepository,
    @MakeSnackbarVisualsFlow private val sharedSnackbarVisuals: MutableSharedFlow<(Context) -> SnackbarVisuals>,
    @ApplicationContext context: Context,
    @WidgetPinSuccessFlow val widgetPinSuccessFlow: SharedFlow<Unit>
) :
    ViewModel() {

    val propertyReorderingDiscoveryShown =
        preferencesRepository.propertyReorderingDiscoveryShown.stateIn(viewModelScope, SharingStarted.WhileSubscribed()) { true }

    fun savePropertyReorderingDiscoveryShown() {
        viewModelScope.launch {
            preferencesRepository.propertyReorderingDiscoveryShown.save(true)
        }
    }

    // =========
    // Pinning
    // =========

    fun attemptWidgetPin(context: Context) {
        appWidgetManager.attemptWifiWidgetPin(
            context = context,
            onFailure = {
                viewModelScope.launch {
                    sharedSnackbarVisuals.emit {
                        AppSnackbarVisuals(
                            msg = it.getString(R.string.widget_pinning_failed),
                            kind = SnackbarKind.Warning
                        )
                    }
                }
            }
        )
    }

    // =========
    // Configuration
    // =========

    private val refreshInterval = repository.refreshInterval.reversibleStateFlow(
        scope = viewModelScope,
        started = SharingStarted.Eagerly
    )

    val configuration = ReversibleWidgetConfiguration(
        coloringConfig = ReversibleStateFlow(
            scope = viewModelScope,
            appliedStateFlow = repository.coloringConfig.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = WidgetColoring.Config()
            ),
            syncState = { repository.saveColoringConfig(it) }
        ),
        opacity = repository.opacity.reversibleStateFlow(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        ),
        fontSize = repository.fontSize.reversibleStateFlow(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        ),
        wifiProperties = repository.wifiPropertyEnablementMap.reversibleStateMap(scope = viewModelScope),
        orderedWifiProperties = repository.orderedWifiProperties.reversibleStateFlow(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        ),
        ipSubProperties = repository.ipSubPropertyEnablementMap.reversibleStateMap(scope = viewModelScope),
        bottomRowMap = repository.bottomRowElementEnablementMap.reversibleStateMap(scope = viewModelScope),
        refreshInterval = refreshInterval,
        refreshingParametersMap = repository.refreshingParametersEnablementMap.reversibleStateMap(
            scope = viewModelScope,
            onStateSynced = {
                widgetDataRefreshWorkerManager.applyRefreshingSettings(
                    parameters = it,
                    interval = refreshInterval.value
                )
            }
        ),
        scope = viewModelScope,
        onStateSynced = {
            WifiWidgetProvider.triggerDataRefresh(context).log { "Triggered widget data refresh " }
            delay(500) // To allow fab buttons to disappear before emission of snackbar
            sharedSnackbarVisuals.emit {
                AppSnackbarVisuals(
                    msg = it.getString(R.string.updated_widget_configuration),
                    kind = SnackbarKind.Success
                )
            }
        }
    )
}
