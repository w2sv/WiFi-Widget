package com.w2sv.wifiwidget.ui.screen.home

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.core.common.R
import com.w2sv.domain.repository.WidgetConfigDataSource
import com.w2sv.widget.di.WidgetPinSuccessFlow
import com.w2sv.widget.utils.attemptWifiWidgetPin
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.screen.home.model.WifiStateProvider
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val widgetConfigDataSource: WidgetConfigDataSource,
    private val appWidgetManager: AppWidgetManager,
    @WidgetPinSuccessFlow val widgetPinSuccessFlow: SharedFlow<Unit>,
    @ApplicationContext val context: Context,
    wifiStateProvider: WifiStateProvider
) : ViewModel(),
    WifiStateProvider by wifiStateProvider {

    private val _snackbarBuilder = MutableSharedFlow<SnackbarBuilder>()
    val snackbarBuilder = _snackbarBuilder.asSharedFlow()

    private val _isAnyLocationAccessRequiringPropertyEnabled = widgetConfigDataSource.config
        .map { it.isAnyLocationAccessRequiringPropertyEnabled }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false
        )
    val isAnyLocationAccessRequiringPropertyEnabled: Boolean get() = _isAnyLocationAccessRequiringPropertyEnabled.value

    fun pinWidget() {
        appWidgetManager.attemptWifiWidgetPin(
            context = context,
            onFailure = {
                viewModelScope.launch {
                    _snackbarBuilder.emit {
                        AppSnackbarVisuals(
                            msg = getString(R.string.widget_pinning_failed),
                            kind = SnackbarKind.Warning
                        )
                    }
                }
            }
        )
    }

    fun enableLocationAccessRequiringProperties() {
        viewModelScope.launch { widgetConfigDataSource.update { it.withEnabledLocationAccessRequiringProperties() } }
    }
}
