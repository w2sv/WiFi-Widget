package com.w2sv.wifiwidget.ui.sharedviewmodel

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.common.utils.log
import com.w2sv.domain.repository.WidgetConfigDataSource
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.widget.WifiWidgetRefreshManager
import com.w2sv.widget.di.WidgetPinSuccessFlow
import com.w2sv.widget.utils.attemptWifiWidgetPin
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.ReversibleWidgetConfig
import com.w2sv.wifiwidget.ui.snackbarvisuals.EmitSnackbarBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetViewModel @Inject constructor(
    repository: WidgetConfigDataSource,
    private val widgetRefreshManager: WifiWidgetRefreshManager,
    private val appWidgetManager: AppWidgetManager,
    private val emitSnackbarBuilder: EmitSnackbarBuilder,
    @ApplicationContext context: Context,
    @WidgetPinSuccessFlow val widgetPinSuccessFlow: SharedFlow<Unit>
) : ViewModel() {

    fun attemptWidgetPin(context: Context) {
        appWidgetManager.attemptWifiWidgetPin(
            context = context,
            onFailure = {
                viewModelScope.launch {
                    emitSnackbarBuilder {
                        AppSnackbarVisuals(
                            msg = getString(R.string.widget_pinning_failed),
                            kind = SnackbarKind.Warning
                        )
                    }
                }
            }
        )
    }

    val configuration = ReversibleWidgetConfig(
        scope = viewModelScope,
        dataSource = repository,
        onStateSynced = {
            WifiWidgetProvider.triggerDataRefresh(context).log { "Triggered widget data refresh on configuration state sync" }
            delay(500) // To allow fab buttons to disappear before emission of snackbar
            emitSnackbarBuilder {
                AppSnackbarVisuals(
                    msg = getString(R.string.updated_widget_configuration),
                    kind = SnackbarKind.Success
                )
            }
        }
    )
}
