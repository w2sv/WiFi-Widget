package com.w2sv.wifiwidget.ui.sharedviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.model.Theme
import com.w2sv.domain.repository.PermissionRepository
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.wifiwidget.di.MakeSnackbarVisuals
import com.w2sv.wifiwidget.di.MutableMakeSnackbarVisualsFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(
    @MutableMakeSnackbarVisualsFlow makeSnackbarVisualsFlow: MutableSharedFlow<MakeSnackbarVisuals>,
    private val permissionRepository: PermissionRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val makeSnackbarVisualsFlow = makeSnackbarVisualsFlow.asSharedFlow()

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
