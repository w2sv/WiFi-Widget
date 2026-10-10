package com.w2sv.wifiwidget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.model.ThemeSettings
import com.w2sv.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ActivityViewModel @Inject constructor(private val preferencesRepository: PreferencesRepository) : ViewModel() {

    val themeSettings = preferencesRepository.themeSettings.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ThemeSettings.Default
    )

    fun updateThemeSettings(transform: (ThemeSettings) -> ThemeSettings) {
        viewModelScope.launch {
            preferencesRepository.updateThemeSettings(transform)
        }
    }
}
