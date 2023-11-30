package com.w2sv.wifiwidget.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.eventhandling.BackPressHandler
import com.w2sv.domain.model.Theme
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.utils.SHARING_STARTED_WHILE_SUBSCRIBED_TIMEOUT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val BACK_PRESS_CONFIRMATION_WINDOW = 2500L

@HiltViewModel
class AppViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) :
    ViewModel() {

    val theme = preferencesRepository.inAppTheme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SHARING_STARTED_WHILE_SUBSCRIBED_TIMEOUT)
    )

    val useDynamicColors = preferencesRepository.useDynamicTheme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SHARING_STARTED_WHILE_SUBSCRIBED_TIMEOUT)
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
        viewModelScope,
        BACK_PRESS_CONFIRMATION_WINDOW
    )
}
