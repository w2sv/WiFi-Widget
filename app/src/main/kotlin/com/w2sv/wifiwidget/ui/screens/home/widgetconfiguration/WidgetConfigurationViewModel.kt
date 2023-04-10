package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.extensions.reset
import com.w2sv.common.Theme
import com.w2sv.common.WidgetColorSection
import com.w2sv.common.WifiProperty
import com.w2sv.common.preferences.CustomWidgetColors
import com.w2sv.common.preferences.DataStoreRepository
import com.w2sv.common.preferences.PreferencesKey
import com.w2sv.common.preferences.WidgetRefreshingParameters
import com.w2sv.common.preferences.WifiProperties
import com.w2sv.widget.WidgetProvider
import com.w2sv.wifiwidget.ui.CoherentNonAppliedStates
import com.w2sv.wifiwidget.ui.NonAppliedSnapshotStateMap
import com.w2sv.wifiwidget.ui.NonAppliedStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

@HiltViewModel
class WidgetConfigurationViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val wifiProperties: WifiProperties,
    private val widgetRefreshingParameters: WidgetRefreshingParameters,
    private val customWidgetColors: CustomWidgetColors,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
     * onSplashScreenAnimationFinished
     */

    fun onSplashScreenAnimationFinished() {
        if (openConfigurationDialogOnSplashScreenAnimationFinished) {
            showWidgetConfigurationDialog.value = true
        }
    }

    private val openConfigurationDialogOnSplashScreenAnimationFinished =
        savedStateHandle.contains(WidgetProvider.EXTRA_OPEN_CONFIGURATION_DIALOG_ON_START)

    val showWidgetConfigurationDialog = MutableStateFlow(false)

    val propertyInfoDialogIndex: MutableStateFlow<Int?> = MutableStateFlow(null)

    val widgetPropertyStateMap = NonAppliedSnapshotStateMap(
        { wifiProperties },
        { wifiProperties.putAll(it) }
    )

    val widgetThemeState = NonAppliedStateFlow(
        viewModelScope,
        dataStoreRepository.widgetTheme,
        { dataStoreRepository.save(it, PreferencesKey.IN_APP_THEME, viewModelScope) }
    )

    val customThemeSelected = widgetThemeState.transform {
        emit(it == Theme.Custom)
    }

    val customWidgetColorsState = NonAppliedSnapshotStateMap(
        { customWidgetColors },
        { customWidgetColors.putAll(it) }
    )

    val customizationDialogSection = MutableStateFlow<WidgetColorSection?>(null)

    fun onDismissCustomizationDialog() {
        customizationDialogSection.reset()
    }

    val widgetOpacityState = NonAppliedStateFlow(
        viewModelScope,
        dataStoreRepository.opacity,
        { dataStoreRepository.save(it, PreferencesKey.OPACITY, viewModelScope) }
    )

    val widgetRefreshingParametersState = NonAppliedSnapshotStateMap(
        { widgetRefreshingParameters },
        {
            widgetRefreshingParameters.putAll(it)
            widgetRefreshingParametersChanged.value = true
        }
    )

    val widgetRefreshingParametersChanged = MutableStateFlow(false)

    val widgetConfigurationStates = CoherentNonAppliedStates(
        widgetPropertyStateMap,
        widgetThemeState,
        widgetOpacityState,
        widgetRefreshingParametersState,
        customWidgetColorsState,
        coroutineScope = viewModelScope
    )

    fun onDismissWidgetConfigurationDialog() {
        widgetConfigurationStates.reset()
        showWidgetConfigurationDialog.value = false
    }

    /**
     * @return Boolean indicating whether change has been confirmed
     */
    fun confirmAndSyncPropertyChange(
        property: WifiProperty,
        value: Boolean,
        onChangeRejected: () -> Unit
    ) {
        when (value || widgetPropertyStateMap.values.count { true } != 1) {
            true -> widgetPropertyStateMap[property.name] = value
            false -> onChangeRejected()
        }
    }
}