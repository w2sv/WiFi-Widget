package com.w2sv.wifiwidget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.model.Theme
import com.w2sv.domain.repository.PermissionRepository
import com.w2sv.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(
    private val permissionRepository: PermissionRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val theme = preferencesRepository.inAppTheme.stateIn(
        viewModelScope,
        SharingStarted.Eagerly
    )

    fun saveTheme(theme: Theme) {
        viewModelScope.launch {
            preferencesRepository.inAppTheme.save(theme)
        }
    }

    val useDynamicColors = preferencesRepository.useDynamicTheme.stateIn(
        viewModelScope,
        SharingStarted.Eagerly
    )

    fun saveUseDynamicColors(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.useDynamicTheme.save(value)
        }
    }

    val useAmoledBlackTheme = preferencesRepository.useAmoledBlackTheme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed()
    )

    fun saveUseAmoledBlackTheme(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.useAmoledBlackTheme.save(value)
        }
    }

    // ===============
    // Permissions
    // ===============

    val locationAccessPermissionRequested by permissionRepository::locationAccessPermissionRequested

    fun saveLocationAccessPermissionRequested() {
        viewModelScope.launch { permissionRepository.locationAccessPermissionRequested.save(true) }
    }

    val locationAccessRationalShown by permissionRepository::locationAccessPermissionRationalShown

    fun saveLocationAccessRationalShown() {
        viewModelScope.launch { permissionRepository.locationAccessPermissionRationalShown.save(true) }
    }
}
