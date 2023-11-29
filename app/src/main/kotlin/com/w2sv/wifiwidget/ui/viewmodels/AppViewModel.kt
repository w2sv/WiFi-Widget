package com.w2sv.wifiwidget.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.eventhandling.BackPressHandler
import com.w2sv.androidutils.notifying.showToast
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

@HiltViewModel
class AppViewModel @Inject constructor(private val preferencesRepository: PreferencesRepository) :
    ViewModel() {

    val inAppTheme = preferencesRepository.inAppTheme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SHARING_STARTED_WHILE_SUBSCRIBED_TIMEOUT)
    )

    val useDynamicTheme = preferencesRepository.useDynamicTheme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SHARING_STARTED_WHILE_SUBSCRIBED_TIMEOUT)
    )

    fun saveInAppTheme(theme: Theme) {
        viewModelScope.launch {
            preferencesRepository.inAppTheme.save(theme)
        }
    }

    fun saveUseDynamicTheme(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.useDynamicTheme.save(value)
        }
    }

    // ==============
    // BackPress Handling
    // ==============

    val exitApplication get() = _exitApplication.asSharedFlow()
    private val _exitApplication = MutableSharedFlow<Unit>()

    fun onBackPress(context: Context) {
        backPressHandler.invoke(
            onFirstPress = {
                context.showToast(context.getString(R.string.tap_again_to_exit))
            },
            onSecondPress = {
                viewModelScope.launch {
                    _exitApplication.emit(Unit)
                }
            },
        )
    }

    private val backPressHandler = BackPressHandler(
        viewModelScope,
        2500L,
    )
}
