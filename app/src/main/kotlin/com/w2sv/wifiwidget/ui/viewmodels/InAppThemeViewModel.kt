package com.w2sv.wifiwidget.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.data.model.Theme
import com.w2sv.data.storage.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InAppThemeViewModel @Inject constructor(private val preferencesRepository: PreferencesRepository) :
    ViewModel() {

    val inAppTheme by preferencesRepository::inAppTheme
    val useDynamicTheme by preferencesRepository::useDynamicTheme

    fun saveInAppTheme(theme: Theme) {
        viewModelScope.launch {
            preferencesRepository.saveInAppTheme(theme)
        }
    }

    fun saveUseDynamicTheme(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveUseDynamicTheme(value)
        }
    }
}