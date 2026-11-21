package com.w2sv.wifiwidget.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.core.resources.R
import com.w2sv.domain.widget.WidgetActions
import com.w2sv.domain.widget.WidgetStatus
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.screen.home.model.wifistate.WifiStateProvider
import com.w2sv.wifiwidget.ui.screen.home.synchronization.RemoteDataSynchronizer
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val widgetActions: WidgetActions,
    widgetStatus: WidgetStatus,
    private val remoteDataSynchronizer: RemoteDataSynchronizer,
    wifiStateProvider: WifiStateProvider
) : ViewModel(),
    WifiStateProvider by wifiStateProvider {
    val snackbarBuilder: SharedFlow<SnackbarBuilder>
        field = MutableSharedFlow<SnackbarBuilder>()
    val widgetPinSuccessFlow = widgetStatus.pinSuccess

    fun onResume() {
        viewModelScope.launch { remoteDataSynchronizer.recoverOnResume() }
    }

    fun pinWidget() {
        viewModelScope.launch {
            val successfullyPinned = widgetActions.pin()
            if (!successfullyPinned) {
                snackbarBuilder.emit {
                    AppSnackbarVisuals(
                        msg = getString(R.string.widget_pinning_failed),
                        kind = SnackbarKind.Warning
                    )
                }
            }
        }
    }
}
