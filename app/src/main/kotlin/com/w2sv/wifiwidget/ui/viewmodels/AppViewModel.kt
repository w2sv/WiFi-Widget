package com.w2sv.wifiwidget.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.eventhandling.BackPressHandler
import com.w2sv.domain.model.Theme
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.di.MutableSharedSnackbarVisualsFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    mutableSharedSnackbarVisuals: MutableSharedSnackbarVisualsFlow,
    private val preferencesRepository: PreferencesRepository
) :
    ViewModel() {

    val sharedSnackbarVisuals = mutableSharedSnackbarVisuals.asSharedFlow()

    val theme = preferencesRepository.inAppTheme.stateIn(
        viewModelScope,
        SharingStarted.Eagerly
    )

    val useDynamicColors = preferencesRepository.useDynamicTheme.stateIn(
        viewModelScope,
        SharingStarted.Eagerly
    )

    fun saveTheme(theme: Theme) {
        viewModelScope.launch {
            preferencesRepository.inAppTheme.save(theme)
        }
    }

    fun saveUseDynamicColors(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.useDynamicTheme.save(value)
        }
    }

    // ==============
    // BackPress Handling
    // ==============

    val exitApplication get() = _exitApplication.asSharedFlow()
    private val _exitApplication = MutableSharedFlow<Unit>()

    /**
     * @return Optional toast message string resource id.
     */
    fun onBackPress(): Int? {
        var messageResId: Int? = null

        backPressHandler.invoke(
            onFirstPress = {
                messageResId = R.string.tap_again_to_exit
            },
            onSecondPress = {
                viewModelScope.launch {
                    _exitApplication.emit(Unit)
                }
            },
        )

        return messageResId
    }

    private val backPressHandler = BackPressHandler(
        coroutineScope = viewModelScope,
        confirmationWindowDuration = 2500L
    )
}
